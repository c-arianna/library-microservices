package mentoring.acomi.loanservice.infrastructure.messaging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer.BookBorrowRejectedIntegrationPayload;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer.BookLoanIntegrationPayload;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer.BookReservationRejectedIntegrationPayload;
import mentoring.acomi.loanservice.application.services.LoanEventService;
import mentoring.acomi.sharedlibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedlibrary.integration.messaging.MessagingTopology;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer.UserIntegrationPayload;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer.UserSubscribedIntegrationPayload;
import tools.jackson.databind.ObjectMapper;

@Component
public class LoanIntegrationEventListener {

	private final ObjectMapper mapper;
	private final LoanEventService service;

	private final Logger logger = LogManager.getLogger(LoanIntegrationEventListener.class);
	
	public LoanIntegrationEventListener(ObjectMapper mapper, LoanEventService service) {
		this.mapper = mapper;
		this.service = service;
	}

	@RabbitListener(queues = MessagingTopology.LOAN_QUEUE)
	public void onEvent(IntegrationEventEnvelope<?> eventEnvelope) {

		try {
			switch (eventEnvelope.eventType()) {
			case BOOK_RESERVED -> {
				BookLoanIntegrationPayload payload = mapper.convertValue(eventEnvelope.payload(),
						BookLoanIntegrationPayload.class);
				service.handleBookReserved(payload);
			}

			case BOOK_RESERVATION_REJECTED -> {
				BookReservationRejectedIntegrationPayload payload = mapper.convertValue(eventEnvelope.payload(),
						BookReservationRejectedIntegrationPayload.class);
				service.handleBookReservationRejected(payload);
			}

			case BOOK_BORROWED -> {
				BookLoanIntegrationPayload payload = mapper.convertValue(eventEnvelope.payload(),
						BookLoanIntegrationPayload.class);
				service.handleBookBorrowed(payload);
			}

			case BOOK_BORROW_REJECTED -> {
				BookBorrowRejectedIntegrationPayload payload = mapper.convertValue(eventEnvelope.payload(),
						BookBorrowRejectedIntegrationPayload.class);
				service.handleBookBorrowRejected(payload);
			}
			case USER_SUBSCRIBED -> {
				UserSubscribedIntegrationPayload payload = mapper.convertValue(eventEnvelope.payload(),
						UserSubscribedIntegrationPayload.class);
				service.handleSubscribeUser(payload);
			}
			
			case USER_UNSUBSCRIBED, USER_SUSPENDED, USER_UNSUSPENDED -> {
				UserIntegrationPayload payload = mapper.convertValue(eventEnvelope.payload(),
						UserIntegrationPayload.class);
				service.handleUpdateUserStatus(payload);
			}
			
			default ->
				throw new IllegalArgumentException(String.format("Unexpected value: %s", eventEnvelope.eventType()));
			}

		} catch (Exception e) {
			logger.error("Failed to process event {}", eventEnvelope.eventType(), e);
			throw e;
		}

	}

}
