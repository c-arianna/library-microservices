package mentoring.acomi.bookservice.infrastructure.messaging;

import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import mentoring.acomi.bookservice.application.errors.NonRetryableEventException;
import mentoring.acomi.bookservice.application.projection.BookProjection;
import mentoring.acomi.bookservice.application.reactor.BookEventReactor;
import mentoring.acomi.bookservice.application.reactor.command.CommandLoanEvent;
import mentoring.acomi.bookservice.application.repositories.BookEventRepository;
import mentoring.acomi.bookservice.domain.events.AggregateType;
import mentoring.acomi.bookservice.domain.events.BookEvent;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.consumer.LoanIntegrationPayload;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.producer.BookCopiesUpdatedIntegrationPayload;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.producer.BookLoanIntegrationPayload;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.producer.BookRegisteredIntegrationPayload;
import mentoring.acomi.bookservice.infrastructure.persistence.entity.BookEventEntity;
import mentoring.acomi.bookservice.infrastructure.persistence.repositories.BookIntegrationRepository;
import mentoring.acomi.sharedlibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedlibrary.integration.messaging.IntegrationEventTypes;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
public class BookEventProcessor {

	private final BookEventReactor reactor;
	private final BookProjection projection;
	private final BookIntegrationRepository bookIntegrationRepository;
	private final BookEventRepository bookEventRepository;
	private final ObjectMapper mapper;
	
	private final BookIntegrationEventMapper eventMapper;
	
	private final Logger logger = LogManager.getLogger(BookEventProcessor.class);
	
	public static final Map<IntegrationEventTypes, Integer> consumerSupportedVersion = Map.ofEntries(
			Map.entry(IntegrationEventTypes.LOAN_REQUESTED, BookIntegrationConsumerEventVersions.LOAN_REQUESTED),
			Map.entry(IntegrationEventTypes.LOAN_CONFIRM_REQUESTED,
					BookIntegrationConsumerEventVersions.LOAN_CONFIRM_REQUESTED),
			Map.entry(IntegrationEventTypes.LOAN_CANCELED, BookIntegrationConsumerEventVersions.LOAN_CANCELED),
			Map.entry(IntegrationEventTypes.LOAN_RETURNED, BookIntegrationConsumerEventVersions.LOAN_RETURNED),
			Map.entry(IntegrationEventTypes.BOOK_REGISTERED, BookIntegrationConsumerEventVersions.BOOK_REGISTERED),
			Map.entry(IntegrationEventTypes.BOOK_COPIES_UPDATED,
					BookIntegrationConsumerEventVersions.BOOK_COPIES_UPDATED),
			Map.entry(IntegrationEventTypes.BOOK_RESERVED, BookIntegrationConsumerEventVersions.BOOK_RESERVED),
			Map.entry(IntegrationEventTypes.BOOK_BORROWED, BookIntegrationConsumerEventVersions.BOOK_BORROWED),
			Map.entry(IntegrationEventTypes.BOOK_RELEASED, BookIntegrationConsumerEventVersions.BOOK_RELEASED),
			Map.entry(IntegrationEventTypes.BOOK_RETURNED, BookIntegrationConsumerEventVersions.BOOK_RETURNED),
			Map.entry(IntegrationEventTypes.BOOK_RESERVATION_REJECTED,
					BookIntegrationConsumerEventVersions.BOOK_RESERVATION_REJECTED),
			Map.entry(IntegrationEventTypes.BOOK_BORROW_REJECTED,
					BookIntegrationConsumerEventVersions.BOOK_BORROW_REJECTED));
	
	public BookEventProcessor(BookEventReactor reactor, BookProjection projection, BookIntegrationRepository bookIntegrationRepository, 
			BookEventRepository bookEventRepository, BookIntegrationEventMapper eventMapper, ObjectMapper mapper) {
		this.reactor = reactor;
		this.projection = projection;
		this.bookIntegrationRepository = bookIntegrationRepository;
		this.bookEventRepository = bookEventRepository;
		this.eventMapper = eventMapper;
		this.mapper = mapper;
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void processConsumerEvent(IntegrationEventEnvelope<?> event, LoanIntegrationPayload payload) {

		if (bookIntegrationRepository.exists(event.eventId(), AggregateType.LOAN.name())) {
			return;
		}

		validatePayload(event.eventType(), payload);

		try {
			bookIntegrationRepository.save(event);
		} catch (DataIntegrityViolationException e) {
			return;
		}
				
		String aggregateId = event.aggregateId();
		String aggregateType = event.aggregateType();
		
		int lastEventVersionProcessed = bookEventRepository.findMaxProcessedVersion(aggregateId, aggregateType).orElse(-1);
		int eventVersion = event.eventVersion();
		
		if(eventVersion == lastEventVersionProcessed + 1) {
			handleConsumerEvent(event.eventId(), event.eventType(), new CommandLoanEvent(payload.loanId(), payload.isbn(), payload.userId()));
			bookEventRepository.markProcessed(event.eventId(), aggregateType);
						
			int nextEventVersionToProcess = eventVersion + 1;
			
			while(true) {
				
				Optional<BookEventEntity> nextEventToProcess = bookIntegrationRepository.findNextEventToProcess(aggregateId, aggregateType, nextEventVersionToProcess);
				
				if(nextEventToProcess.isEmpty()) {
					break;
				}
				
				BookEventEntity eventToProcess = nextEventToProcess.get();				
				LoanIntegrationPayload eventPayload = getLoanEventPayload(eventToProcess);
				
				CommandLoanEvent command = new CommandLoanEvent(eventPayload.loanId(), eventPayload.isbn(), eventPayload.userId());
				handleConsumerEvent(eventToProcess.getEventId(), IntegrationEventTypes.valueOf(eventToProcess.getEventType()), command);
				bookEventRepository.markProcessed(eventToProcess.getEventId(), eventToProcess.getAggregateType());
												
				nextEventVersionToProcess++;
			}
		}
		
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void processProducerEvent( IntegrationEventEnvelope<?> eventEnvelope) {
		
		if (bookEventRepository.existsEventProcessed(eventEnvelope.eventId(), AggregateType.BOOK.name())) {
			return;
		}
		
		String aggregateId = eventEnvelope.aggregateId();
		String aggregateType = eventEnvelope.aggregateType();
		
		int lastEventVersionProcessed = bookEventRepository.findMaxProcessedVersion(aggregateId, aggregateType).orElse(-1);
		int eventVersion = eventEnvelope.eventVersion();
		
		if(eventVersion == lastEventVersionProcessed + 1) {
			
			handleDomainEvent(eventEnvelope);
			bookEventRepository.markProcessed(eventEnvelope.eventId(), aggregateType);
								
			int nextEventVersionToProcess = eventVersion + 1;
			
			while(true) {
				
				Optional<BookEvent> nextEventToProcess = bookEventRepository.findNextEventToProcess(aggregateId, aggregateType, nextEventVersionToProcess);
				
				if(nextEventToProcess.isEmpty()) {
					break;
				}
				
				BookEvent eventToProcess = nextEventToProcess.get();
				handleDomainEvent(eventMapper.map(eventToProcess));
				bookEventRepository.markProcessed(eventToProcess.eventId(), eventToProcess.aggregateType());
				
				nextEventVersionToProcess++;
			}
		}
		
	}

	private void handleConsumerEvent(String eventId, IntegrationEventTypes eventType, CommandLoanEvent command) {
		logger.info("Processing event {}, ID: {}", eventType, eventId);
		reactor.handle(eventType, command);
		logger.info("Event processed successfully");
	}
	
	private void handleDomainEvent(IntegrationEventEnvelope<?> eventEnvelope) {
		
		logger.info("Processing event {}, ID: {}", eventEnvelope.eventType().eventName, eventEnvelope.eventId());
		
		try {
			switch (eventEnvelope.eventType()) {
			
				case BOOK_REGISTERED -> {
					projection.addBook(mapper.convertValue(eventEnvelope.payload(), BookRegisteredIntegrationPayload.class), eventEnvelope.occurredAt());
				}
		
				case BOOK_COPIES_UPDATED -> {
					projection.updateCopies(mapper.convertValue(eventEnvelope.payload(), BookCopiesUpdatedIntegrationPayload.class), eventEnvelope.occurredAt());
				}
		
				case BOOK_RESERVED -> {
					projection.reserve(mapper.convertValue(eventEnvelope.payload(), BookLoanIntegrationPayload.class), eventEnvelope.occurredAt());
				}
		
				case BOOK_BORROWED -> {
					projection.borrow(mapper.convertValue(eventEnvelope.payload(), BookLoanIntegrationPayload.class), eventEnvelope.occurredAt());
				}
		
				case BOOK_RELEASED -> {
					projection.release(mapper.convertValue(eventEnvelope.payload(), BookLoanIntegrationPayload.class), eventEnvelope.occurredAt());
				}
		
				case BOOK_RETURNED -> {
					projection.returnBorrowed(mapper.convertValue(eventEnvelope.payload(), BookLoanIntegrationPayload.class), eventEnvelope.occurredAt());
				}
		
				default ->
					throw new NonRetryableEventException(String.format("Unknown event type: %s", eventEnvelope.eventType()));
				}
			logger.info("Event processed successfully");
		} catch (Exception e) {
			logger.error("Failed to process event {}", eventEnvelope.eventType(), e);
			throw e;
		}
	}
	
    private LoanIntegrationPayload getLoanEventPayload(BookEventEntity eventToProcess) {
		
		int supportedVersion = consumerSupportedVersion.getOrDefault(IntegrationEventTypes.valueOf(eventToProcess.getEventType()), -1);
		
        if(eventToProcess.getSchemaVersion() == supportedVersion) {
        	return mapper.convertValue(eventToProcess.getPayload(), LoanIntegrationPayload.class);
        }
        
        JsonNode jsonPayload = mapper.valueToTree(eventToProcess.getPayload());
        
        String loanId = getPayloadField(eventToProcess.getEventType(), jsonPayload, "loanId");
        
        String isbn = getPayloadField(eventToProcess.getEventType(), jsonPayload, "isbn");
             
        String userId = getPayloadField(eventToProcess.getEventType(), jsonPayload, "userId");
                
		return new LoanIntegrationPayload(loanId, isbn, userId);
	}

	private void validatePayload(IntegrationEventTypes eventType, LoanIntegrationPayload payload) {

		if (payload == null || payload.loanId() == null || payload.userId() == null || payload.isbn() == null) {
			throw new NonRetryableEventException(String.format("Invalid payload for event %s", eventType));
		}
	}
	
    private String getPayloadField(String eventType, JsonNode jsonPayload, String field) {
		
		String fieldValue = jsonPayload.get(field).asString();

		if (fieldValue == null || fieldValue.isBlank()) {
			throw new NonRetryableEventException(String.format("Missing payload field %s for event %s", field, eventType));
		}
		
		return fieldValue;
	}

}
