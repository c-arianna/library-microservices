package mentoring.acomi.loanservice.infrastructure.messaging;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import mentoring.acomi.loanservice.application.errors.NonRetryableEventException;
import mentoring.acomi.loanservice.application.projection.LoanProjection;
import mentoring.acomi.loanservice.application.projection.UserProjection;
import mentoring.acomi.loanservice.application.reactor.LoanEventReactor;
import mentoring.acomi.loanservice.application.reactor.command.CommandBookEvent;
import mentoring.acomi.loanservice.application.reactor.command.CommandBookRejectedEvent;
import mentoring.acomi.loanservice.application.repositories.LoanEventRepository;
import mentoring.acomi.loanservice.domain.errors.InvalidLoanStateTransition;
import mentoring.acomi.loanservice.domain.events.AggregateType;
import mentoring.acomi.loanservice.domain.events.LoanEvent;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer.BookBorrowRejectedIntegrationPayload;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer.BookLoanIntegrationPayload;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer.BookReservationRejectedIntegrationPayload;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer.UserIntegrationPayload;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer.UserSubscribedIntegrationPayload;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.producer.LoanIntegrationPayload;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.producer.LoanRequestedIntegrationPayload;
import mentoring.acomi.loanservice.infrastructure.persistence.entity.LoanEventEntity;
import mentoring.acomi.loanservice.infrastructure.persistence.repositories.LoanIntegrationRepository;
import mentoring.acomi.sharedlibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedlibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.sharedlibrary.model.UserRole;
import mentoring.acomi.sharedlibrary.model.UserStatus;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
public class LoanEventProcessor {

	private final LoanEventReactor reactor;
	private final LoanProjection projection;
	private final LoanIntegrationRepository loanIntegrationRepository;
	private final LoanEventRepository loanEventRepository;
	private final ObjectMapper mapper;
	private final UserProjection userProjection;

	private final LoanIntegrationEventMapper eventMapper;

	private List<IntegrationEventTypes> reactorConsumerEvents = List.of(IntegrationEventTypes.BOOK_RESERVED, IntegrationEventTypes.BOOK_RESERVATION_REJECTED,
			IntegrationEventTypes.BOOK_BORROWED, IntegrationEventTypes.BOOK_BORROW_REJECTED);
	
	
	public static final Map<IntegrationEventTypes, Integer> consumerSupportedVersion = Map.ofEntries(
			Map.entry(IntegrationEventTypes.BOOK_RESERVED, LoanIntegrationConsumerEventVersions.BOOK_RESERVED),
			Map.entry(IntegrationEventTypes.BOOK_RESERVATION_REJECTED, LoanIntegrationConsumerEventVersions.BOOK_RESERVATION_REJECTED),
			Map.entry(IntegrationEventTypes.BOOK_BORROWED, LoanIntegrationConsumerEventVersions.BOOK_BORROWED),
			Map.entry(IntegrationEventTypes.BOOK_BORROW_REJECTED, LoanIntegrationConsumerEventVersions.BOOK_BORROW_REJECTED),
			Map.entry(IntegrationEventTypes.USER_SUBSCRIBED, LoanIntegrationConsumerEventVersions.USER_SUBSCRIBED),
			Map.entry(IntegrationEventTypes.USER_UNSUBSCRIBED, LoanIntegrationConsumerEventVersions.USER_UNSUBSCRIBED),
			Map.entry(IntegrationEventTypes.USER_SUSPENDED, LoanIntegrationConsumerEventVersions.USER_SUSPENDED),
			Map.entry(IntegrationEventTypes.USER_UNSUSPENDED, LoanIntegrationConsumerEventVersions.USER_UNSUSPENDED),
			Map.entry(IntegrationEventTypes.LOAN_REQUESTED, LoanIntegrationConsumerEventVersions.LOAN_REQUESTED),
			Map.entry(IntegrationEventTypes.LOAN_CONFIRMED, LoanIntegrationConsumerEventVersions.LOAN_CONFIRMED),
			Map.entry(IntegrationEventTypes.LOAN_CANCELED, LoanIntegrationConsumerEventVersions.LOAN_CANCELED),
			Map.entry(IntegrationEventTypes.LOAN_RETURNED, LoanIntegrationConsumerEventVersions.LOAN_RETURNED),
			Map.entry(IntegrationEventTypes.LOAN_RESERVED, LoanIntegrationConsumerEventVersions.LOAN_RESERVED),
			Map.entry(IntegrationEventTypes.LOAN_FAILED, LoanIntegrationConsumerEventVersions.LOAN_FAILED));
	
	private static final int MAX_ITERATIONS = 10;
	private static final int MAX_RETRY_PER_EVENT = 5;
	
	private final Logger logger = LogManager.getLogger(LoanEventProcessor.class);

	public LoanEventProcessor(LoanEventReactor reactor, LoanProjection projection,
			LoanIntegrationRepository loanIntegrationRepository, LoanEventRepository loanEventRepository,
			ObjectMapper mapper, LoanIntegrationEventMapper eventMapper, UserProjection userProjection) {
		this.reactor = reactor;
		this.projection = projection;
		this.loanIntegrationRepository = loanIntegrationRepository;
		this.loanEventRepository = loanEventRepository;
		this.mapper = mapper;
		this.eventMapper = eventMapper;
		this.userProjection = userProjection;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void processConsumerEvent(IntegrationEventEnvelope<?> event) {

		if (loanIntegrationRepository.exists(event.eventId(), event.aggregateType())) {
			return;
		}

		try {
			loanIntegrationRepository.save(event);
		} catch (DataIntegrityViolationException e) {
			return;
		}

		if(isReactorConsumerEvent(event.eventType())) {
			handleReactorConsumerEvent(event);
		}
		else {
			handleProjectionConsumerEvent(event);
		}

	}

	private void handleProjectionConsumerEvent(IntegrationEventEnvelope<?> event) {
		
		String aggregateId = event.aggregateId();
		String aggregateType = event.aggregateType();

		int lastEventVersionProcessed = loanEventRepository.findMaxProcessedVersion(aggregateId, aggregateType).orElse(-1);
		int eventVersion = event.eventVersion();

		if (eventVersion == lastEventVersionProcessed + 1) {

			handleProjectionConsumerEvent(event.eventId(), event.eventType(), getEventPayload(event.eventType(), event.payload()), event.occurredAt());

			loanEventRepository.markProcessed(event.eventId(), aggregateType);

			int nextEventVersionToProcess = eventVersion + 1;

			while (true) {

				Optional<LoanEventEntity> nextEventToProcess = loanIntegrationRepository.findNextEventToProcess(aggregateId, aggregateType, nextEventVersionToProcess);

				if (nextEventToProcess.isEmpty()) {
					break;
				}

				LoanEventEntity eventToProcess = nextEventToProcess.get();
				handleProjectionConsumerEvent(eventToProcess.getEventId(), IntegrationEventTypes.valueOf(eventToProcess.getEventType()), getEventPayload(eventToProcess),
						eventToProcess.getOccurredAt());
				loanEventRepository.markProcessed(eventToProcess.getEventId(), eventToProcess.getAggregateType());

				nextEventVersionToProcess++;
			}
		}
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void processProducerEvent(IntegrationEventEnvelope<?> eventEnvelope) {

		if (loanEventRepository.existsEventProcessed(eventEnvelope.eventId(), AggregateType.LOAN.name())) {
			return;
		}

		String aggregateId = eventEnvelope.aggregateId();
		String aggregateType = eventEnvelope.aggregateType();

		int lastEventVersionProcessed = loanEventRepository.findMaxProcessedVersion(aggregateId, aggregateType).orElse(-1);
		int eventVersion = eventEnvelope.eventVersion();

		if (eventVersion == lastEventVersionProcessed + 1) {

			handleDomainEvent(eventEnvelope);
			loanEventRepository.markProcessed(eventEnvelope.eventId(), aggregateType);

			int nextEventVersionToProcess = eventVersion + 1;

			while (true) {

				Optional<LoanEvent> nextEventToProcess = loanEventRepository.findNextEventToProcess(aggregateId, aggregateType, nextEventVersionToProcess);

				if (nextEventToProcess.isEmpty()) {
					break;
				}

				LoanEvent eventToProcess = nextEventToProcess.get();
				handleDomainEvent(eventMapper.map(eventToProcess));
				loanEventRepository.markProcessed(eventToProcess.eventId(), eventToProcess.aggregateType());

				nextEventVersionToProcess++;
			}
		}

	}

	private void handleDomainEvent(IntegrationEventEnvelope<?> eventEnvelope) {

		logger.info("Processing event {}, ID: {}", eventEnvelope.eventType().eventName, eventEnvelope.eventId());

		try {

			switch (eventEnvelope.eventType()) {

				case LOAN_REQUESTED -> {
					LoanRequestedIntegrationPayload payload = mapper.convertValue(eventEnvelope.payload(), LoanRequestedIntegrationPayload.class);
					projection.loanInsert(payload, eventEnvelope.occurredAt());
				}
	
				case LOAN_CONFIRMED -> {
					LoanIntegrationPayload payload = mapper.convertValue(eventEnvelope.payload(), LoanIntegrationPayload.class);
					projection.confirmLoan(payload, eventEnvelope.occurredAt());
				}
	
				case LOAN_CANCELED -> {
					LoanIntegrationPayload payload = mapper.convertValue(eventEnvelope.payload(), LoanIntegrationPayload.class);
					projection.cancelLoan(payload, eventEnvelope.occurredAt());
				}
	
				case LOAN_RETURNED -> {
					LoanIntegrationPayload payload = mapper.convertValue(eventEnvelope.payload(), LoanIntegrationPayload.class);
					projection.returnLoan(payload, eventEnvelope.occurredAt());
				}
	
				case LOAN_RESERVED -> {
					LoanIntegrationPayload payload = mapper.convertValue(eventEnvelope.payload(), LoanIntegrationPayload.class);
					projection.reserveLoan(payload, eventEnvelope.occurredAt());
				}
	
				case LOAN_FAILED -> {
					LoanIntegrationPayload payload = mapper.convertValue(eventEnvelope.payload(), LoanIntegrationPayload.class);
					projection.failLoan(payload, eventEnvelope.occurredAt());
				}
				
				default -> throw new NonRetryableEventException(String.format("Unknown event type: %s", eventEnvelope.eventType()));
			
			}
			logger.info("Event processed successfully");
		} catch (Exception e) {
			logger.error("Failed to process event {}", eventEnvelope.eventType(), e);
			throw e;
		}

	}

	private void handleProjectionConsumerEvent(String eventId, IntegrationEventTypes eventType, Object payload, Instant occurredAt) {

		logger.info("Processing event {}, ID: {}", eventType, eventId);

		switch (eventType) {

			case USER_SUBSCRIBED -> {
				UserSubscribedIntegrationPayload eventPayload = (UserSubscribedIntegrationPayload) payload;
				userProjection.handleSubscribeUser(eventPayload, occurredAt);
			}
	
			case USER_UNSUBSCRIBED, USER_SUSPENDED, USER_UNSUSPENDED -> {
				UserIntegrationPayload eventPayload = (UserIntegrationPayload) payload;
				userProjection.handleUpdateUserStatus(eventPayload, occurredAt);
			}
	
			default -> throw new NonRetryableEventException(String.format("Unknown event type: %s", eventType));
			}

		logger.info("Event processed successfully");
	}

	private void handleReactorConsumerEvent(IntegrationEventEnvelope<?> event) {
		
		try {
			handleReactorConsumerEvent(event.eventId(), event.eventType(), getEventPayload(event.eventType(), event.payload()), event.occurredAt());
			loanEventRepository.markProcessed(event.eventId(), event.aggregateType());
			retryPendingReactorEvents(event.aggregateId(), extractLoanId(getEventPayload(event.eventType(), event.payload())));
		} catch (InvalidLoanStateTransition e) {
			 logger.warn("Event too early or invalid state, will retry later. Event: {}", event.eventType());
		}catch (Exception e) {
			 logger.error("Unexpected error", e);
			 throw e;
		}
		
	}
	private void handleReactorConsumerEvent(String eventId, IntegrationEventTypes eventType, Object payload, Instant occurredAt) {

		logger.info("Processing event {}, ID: {}", eventType, eventId);

		switch (eventType) {

			case BOOK_RESERVED -> {
				BookLoanIntegrationPayload eventPayload = (BookLoanIntegrationPayload) payload;
				reactor.handleBookReserved(new CommandBookEvent(eventPayload.loanId()));
			}
	
			case BOOK_RESERVATION_REJECTED -> {
				BookReservationRejectedIntegrationPayload eventPayload = (BookReservationRejectedIntegrationPayload) payload;
				reactor.handleBookReservationRejected(new CommandBookRejectedEvent(eventPayload.loanId(), eventPayload.reason()));
			}
	
			case BOOK_BORROWED -> {
				BookLoanIntegrationPayload eventPayload = (BookLoanIntegrationPayload) payload;
				reactor.handleBookBorrowed(new CommandBookEvent(eventPayload.loanId()));
			}
	
			case BOOK_BORROW_REJECTED -> {
				BookBorrowRejectedIntegrationPayload eventPayload = (BookBorrowRejectedIntegrationPayload) payload;
				reactor.handleBookBorrowRejected(new CommandBookRejectedEvent(eventPayload.loanId(), eventPayload.reason()));
			}
	
			default -> throw new NonRetryableEventException(String.format("Unknown event type: %s", eventType));
			}

		logger.info("Event processed successfully");
	}
	
	private BookLoanIntegrationPayload getBookLoanIntegrationPayload(LoanEventEntity eventToProcess) {

		int supportedVersion = consumerSupportedVersion.getOrDefault(IntegrationEventTypes.valueOf(eventToProcess.getEventType()), -1);

		if (eventToProcess.getSchemaVersion() == supportedVersion) {
			return mapper.convertValue(eventToProcess.getPayload(), BookLoanIntegrationPayload.class);
		}

		JsonNode jsonPayload = mapper.valueToTree(eventToProcess.getPayload());

		String loanId = getPayloadField(eventToProcess.getEventType(), jsonPayload, "loanId");

		String isbn = getPayloadField(eventToProcess.getEventType(), jsonPayload, "isbn");

		String userId = getPayloadField(eventToProcess.getEventType(), jsonPayload, "userId");

		return new BookLoanIntegrationPayload(isbn, loanId, userId);
	}

	private BookReservationRejectedIntegrationPayload getBookReservationRejectedIntegrationPayload(LoanEventEntity eventToProcess) {
		
		int supportedVersion = consumerSupportedVersion.getOrDefault(IntegrationEventTypes.valueOf(eventToProcess.getEventType()), -1);

		if (eventToProcess.getSchemaVersion() == supportedVersion) {
			return mapper.convertValue(eventToProcess.getPayload(), BookReservationRejectedIntegrationPayload.class);
		}

		JsonNode jsonPayload = mapper.valueToTree(eventToProcess.getPayload());

		String loanId = getPayloadField(eventToProcess.getEventType(), jsonPayload, "loanId");

		String isbn = getPayloadField(eventToProcess.getEventType(), jsonPayload, "isbn");

		String userId = getPayloadField(eventToProcess.getEventType(), jsonPayload, "userId");

		String reason = getPayloadField(eventToProcess.getEventType(), jsonPayload, "reason");

		return new BookReservationRejectedIntegrationPayload(isbn, loanId, userId, reason);
	}

	private BookBorrowRejectedIntegrationPayload getBookBorrowRejectedIntegrationPayload(LoanEventEntity eventToProcess) {

		int supportedVersion = consumerSupportedVersion.getOrDefault(IntegrationEventTypes.valueOf(eventToProcess.getEventType()), -1);

		if (eventToProcess.getSchemaVersion() == supportedVersion) {
			return mapper.convertValue(eventToProcess.getPayload(), BookBorrowRejectedIntegrationPayload.class);
		}

		JsonNode jsonPayload = mapper.valueToTree(eventToProcess.getPayload());

		String loanId = getPayloadField(eventToProcess.getEventType(), jsonPayload, "loanId");

		String isbn = getPayloadField(eventToProcess.getEventType(), jsonPayload, "isbn");

		String userId = getPayloadField(eventToProcess.getEventType(), jsonPayload, "userId");

		String reason = getPayloadField(eventToProcess.getEventType(), jsonPayload, "reason");

		return new BookBorrowRejectedIntegrationPayload(isbn, loanId, userId, reason);
	}

	private UserIntegrationPayload getUserIntegrationPayload(LoanEventEntity eventToProcess) {

		int supportedVersion = consumerSupportedVersion.getOrDefault(IntegrationEventTypes.valueOf(eventToProcess.getEventType()), -1);

		if (eventToProcess.getSchemaVersion() == supportedVersion) {
			return mapper.convertValue(eventToProcess.getPayload(), UserIntegrationPayload.class);
		}

		JsonNode jsonPayload = mapper.valueToTree(eventToProcess.getPayload());

		String userId = getPayloadField(eventToProcess.getEventType(), jsonPayload, "userId");

		String status = jsonPayload.get("status").asString();

		if (status == null) {
			throw new NonRetryableEventException(String.format("Missing payload field reason for event %s", eventToProcess.getEventType()));
		}

		try {
			UserStatus.valueOf(status);
		} catch (Exception e) {
			throw new NonRetryableEventException(String.format("Invalid payload field status %s for event %s", status, eventToProcess.getEventType()));
		}

		return new UserIntegrationPayload(userId, UserStatus.valueOf(status));
	}

	private UserSubscribedIntegrationPayload getUserSubscribedIntegrationPayload(LoanEventEntity eventToProcess) {
		int supportedVersion = consumerSupportedVersion.getOrDefault(IntegrationEventTypes.valueOf(eventToProcess.getEventType()), -1);

		if (eventToProcess.getSchemaVersion() == supportedVersion) {
			return mapper.convertValue(eventToProcess.getPayload(), UserSubscribedIntegrationPayload.class);
		}

		JsonNode jsonPayload = mapper.valueToTree(eventToProcess.getPayload());

		String userId = getPayloadField(eventToProcess.getEventType(), jsonPayload, "userId");

		String email = getPayloadField(eventToProcess.getEventType(), jsonPayload, "email");

		String name = getPayloadField(eventToProcess.getEventType(), jsonPayload, "name");

		String lastname = getPayloadField(eventToProcess.getEventType(), jsonPayload, "lastname");

		String userIdentityProviderId = getPayloadField(eventToProcess.getEventType(), jsonPayload, "userIdentityProviderId");

		String status = jsonPayload.get("status").asString();

		if (status == null) {
			throw new NonRetryableEventException("Missing payload field status for event %s".formatted(eventToProcess.getEventType()));
		}

		try {
			UserStatus.valueOf(status);
		} catch (Exception e) {
			throw new NonRetryableEventException("Invalid payload field status %s for event %s".formatted(eventToProcess.getEventType()));
		}

		String role = jsonPayload.get("role").asString();

		if (role == null) {
			throw new NonRetryableEventException("Missing payload field role for event %s".formatted(eventToProcess.getEventType()));
		}

		try {
			UserRole.valueOf(role);
		} catch (Exception e) {
			throw new NonRetryableEventException("Invalid payload field role %s for event %s".formatted(role, eventToProcess.getEventType()));
		}

		return new UserSubscribedIntegrationPayload(userId, email, name, lastname, userIdentityProviderId, UserStatus.valueOf(status), UserRole.valueOf(role));
	}

	private Object getEventPayload(LoanEventEntity event) {

		return switch (IntegrationEventTypes.valueOf(event.getEventType())) {
			
			case BOOK_RESERVED -> {
				yield getBookLoanIntegrationPayload(event);
			}
			case BOOK_RESERVATION_REJECTED -> {
				yield getBookReservationRejectedIntegrationPayload(event);
			}
	
			case BOOK_BORROWED -> {
				yield getBookLoanIntegrationPayload(event);
			}
	
			case BOOK_BORROW_REJECTED -> {
				yield getBookBorrowRejectedIntegrationPayload(event);
			}
	
			case USER_SUBSCRIBED -> {
				yield getUserSubscribedIntegrationPayload(event);
			}
	
			case USER_UNSUBSCRIBED, USER_SUSPENDED, USER_UNSUSPENDED -> {
				yield getUserIntegrationPayload(event);
			}
			
			default -> throw new NonRetryableEventException(String.format("Unknown event type: %s", event.getEventType()));
			
		};
	}

	private Object getEventPayload(IntegrationEventTypes eventType, Object payload) {
		
		return switch (eventType) {

			case BOOK_RESERVED -> {
				yield mapper.convertValue(payload, BookLoanIntegrationPayload.class);
			}
	
			case BOOK_RESERVATION_REJECTED -> {
				yield mapper.convertValue(payload, BookReservationRejectedIntegrationPayload.class);
			}
	
			case BOOK_BORROWED -> {
				yield mapper.convertValue(payload, BookLoanIntegrationPayload.class);
			}
	
			case BOOK_BORROW_REJECTED -> {
				yield mapper.convertValue(payload, BookBorrowRejectedIntegrationPayload.class);
			}
			
			case USER_SUBSCRIBED -> {
				yield mapper.convertValue(payload, UserSubscribedIntegrationPayload.class);
			}
	
			case USER_UNSUBSCRIBED, USER_SUSPENDED, USER_UNSUSPENDED -> {
				yield mapper.convertValue(payload, UserIntegrationPayload.class);
			}
	
			default ->
				throw new IllegalArgumentException(String.format("Unknown event type: %s", eventType));
		};
	}
	
    private String getPayloadField(String eventType, JsonNode jsonPayload, String field) {
		
		String fieldValue = jsonPayload.get(field).asString();

		if (fieldValue == null || fieldValue.isBlank()) {
			throw new NonRetryableEventException(String.format("Missing payload field %s for event %s", field, eventType));
		}
		
		return fieldValue;
	}
    
    private boolean isReactorConsumerEvent(IntegrationEventTypes eventType) {
		return reactorConsumerEvents.contains(eventType);
	}
    
    private void retryPendingReactorEvents(String aggregateId, String loanId) {
  
        int iteration = 0;
        
        while (iteration++ < MAX_ITERATIONS) {
        	
        	 boolean progressed = false;
  
        	 List<LoanEventEntity> pendingEvents = findUnprocessedByLoanIdAndTypes(aggregateId, loanId);
        	 
        	 for (LoanEventEntity event : pendingEvents) {

                 if (event.isProcessed() || event.isFailed()) {
                     continue;
                 }
                 
                 if (event.getRetryCount() >= MAX_RETRY_PER_EVENT) {
                     logger.error("Event {} exceeded max retry → mark FAILED", event.getEventId());
                     loanIntegrationRepository.markFailed(event.getEventId(), event.getAggregateType());
                     continue;
                 }

                 try {
                     handleReactorConsumerEvent(event.getEventId(), IntegrationEventTypes.valueOf(event.getEventType()), getEventPayload(event), 
                    		 event.getOccurredAt());

                     loanEventRepository.markProcessed(event.getEventId(), event.getAggregateType());

                     progressed = true;

                 } catch (InvalidLoanStateTransition e) {
                	 loanIntegrationRepository.incrementRetry(event.getEventId(), event.getAggregateType());
                	 int retry = event.getRetryCount() + 1;
                     logger.debug("Event {} still not applicable (retry={})", event.getEventId(), retry);

                 } catch (Exception e) {
                     logger.error("Unexpected error on retry", e);
                     throw e;
                 }
             }

             if (!progressed) {
                 break;
             }
         }

         if (iteration == MAX_ITERATIONS) {
             logger.warn("Reached max iteration loop for loanId={}", loanId);
         }
    }
    
    private String extractLoanId(Object payload) {
        
        if (payload instanceof BookLoanIntegrationPayload p) {
            return p.loanId();
        }

        if (payload instanceof BookReservationRejectedIntegrationPayload p) {
            return p.loanId();
        }

        if (payload instanceof BookBorrowRejectedIntegrationPayload p) {
            return p.loanId();
        }

        throw new IllegalArgumentException("LoanId not found");
    }
  
    private List<LoanEventEntity> findUnprocessedByLoanIdAndTypes(String aggregateId, String loanId) {
        List<LoanEventEntity> events = loanIntegrationRepository.findEventsToProcess(aggregateId, reactorConsumerEvents.stream().map(Enum::name).toList());
        return events.stream().filter(e -> getPayloadField(e.getEventType(), e.getPayload(), "loanId").equals(loanId)).sorted(Comparator.comparing(LoanEventEntity::getOccurredAt)).toList();
    }
    
}
