package mentoring.acomi.loanservice.messaging.handler;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import mentoring.acomi.sharedcodelibrary.event.handlers.InvalidEventPayloadException;
import mentoring.acomi.sharedcodelibrary.event.handlers.EventPayloadMapper;
import mentoring.acomi.loanservice.application.reactor.LoanEventReactor;
import mentoring.acomi.loanservice.application.reactor.command.CommandBookRejectedEvent;
import mentoring.acomi.loanservice.domain.events.AggregateType;
import mentoring.acomi.loanservice.infrastructure.messaging.handlers.BookReservationRejectedV1Handler;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer.BookReservationRejectedIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.EventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
public class BookReservationRejectedV1HandlerTest extends AbstractEventHandlerTest {

	private static final String BOOK_NOT_AVAILABLE = "BOOK_NOT_AVAILABLE";

	@Mock
    private LoanEventReactor reactor;

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();
	
	private EventPayloadMapper mapper;

    private BookReservationRejectedV1Handler handler;

    @BeforeEach
    void setUp() {
    	mapper = new EventPayloadMapper(OBJECT_MAPPER, VALIDATOR);
        handler = new BookReservationRejectedV1Handler(reactor, mapper);
    }

    @Test
    void shouldHandleBookReservationRejectedEvent() {

        IntegrationEventEnvelope<BookReservationRejectedIntegrationPayload> event = validEvent();
        
        handler.handleEvent(event);

        CommandBookRejectedEvent command = new CommandBookRejectedEvent(event.payload().loanId(), event.payload().reason());

        verify(reactor, times(1)).handleBookReservationRejected(command);

    }

    @Test
    void shouldRejectPayloadWithBlankIsbn() {

        IntegrationEventEnvelope<BookReservationRejectedIntegrationPayload> event = getBookReservationRejectedEvent("", 
        		UUID.randomUUID().toString(), UUID.randomUUID().toString(), BOOK_NOT_AVAILABLE, 1);

        InvalidEventPayloadException exception = Assertions.assertThrows(InvalidEventPayloadException.class, () -> handler.handleEvent(event));

		Assertions.assertAll(
	            () -> Assertions.assertEquals(
	                    Set.of("isbn"),
	                    exception.getInvalidFields()),
	            () -> verifyNoInteractions(reactor)
	    );
    }

    @Test
    void shouldRejectPayloadWithBlankLoanId() {

        IntegrationEventEnvelope<BookReservationRejectedIntegrationPayload> event = getBookReservationRejectedEvent("9788804336327", "", 
        		UUID.randomUUID().toString(), BOOK_NOT_AVAILABLE, 1);

        InvalidEventPayloadException exception = Assertions.assertThrows(InvalidEventPayloadException.class, () -> handler.handleEvent(event));

		Assertions.assertAll(
	            () -> Assertions.assertEquals(
	                    Set.of("loanId"),
	                    exception.getInvalidFields()),
	            () -> verifyNoInteractions(reactor)
	    );
    }

    @Test
    void shouldRejectPayloadWithBlankUserId() {

        IntegrationEventEnvelope<BookReservationRejectedIntegrationPayload> event = getBookReservationRejectedEvent("9788804336327",
        		UUID.randomUUID().toString(), "", BOOK_NOT_AVAILABLE, 1);

        InvalidEventPayloadException exception = Assertions.assertThrows(InvalidEventPayloadException.class, () -> handler.handleEvent(event));

		Assertions.assertAll(
	            () -> Assertions.assertEquals(
	                    Set.of("userId"),
	                    exception.getInvalidFields()),
	            () -> verifyNoInteractions(reactor)
	    );
    }
    
    @Test
    void shouldRejectPayloadWithBlankReason() {

        IntegrationEventEnvelope<BookReservationRejectedIntegrationPayload> event = getBookReservationRejectedEvent("9788804336327",
        		UUID.randomUUID().toString(), UUID.randomUUID().toString(), "", 1);

        InvalidEventPayloadException exception = Assertions.assertThrows(InvalidEventPayloadException.class, () -> handler.handleEvent(event));

		Assertions.assertAll(
	            () -> Assertions.assertEquals(
	                    Set.of("reason"),
	                    exception.getInvalidFields()),
	            () -> verifyNoInteractions(reactor)
	    );
    }
    
	@Override
	protected EventHandler handler() {
		return handler;
	}

	@Override
	protected IntegrationEventTypes eventType() {
		return handler.eventType();
	}

	@Override
	protected IntegrationEventEnvelope<BookReservationRejectedIntegrationPayload> validEvent() {
		return getBookReservationRejectedEvent("9788804336327", UUID.randomUUID().toString(), UUID.randomUUID().toString(), BOOK_NOT_AVAILABLE, 1);
	}

	@Override
	protected IntegrationEventEnvelope<?> differentEvent() {
		 return new IntegrationEventEnvelope<>(UUID.randomUUID().toString(), IntegrationEventTypes.BOOK_COPIES_UPDATED,
	                "book-service", "9788804336327", AggregateType.BOOK.name(), 1, Instant.now(), 1, Map.of());
	}

	@Override
	protected IntegrationEventEnvelope<?> withSchemaVersion(int schemaVersion) {
		return getBookReservationRejectedEvent("9788804336327", UUID.randomUUID().toString(), UUID.randomUUID().toString(), BOOK_NOT_AVAILABLE, schemaVersion);
	}
	
	private IntegrationEventEnvelope<BookReservationRejectedIntegrationPayload> getBookReservationRejectedEvent(String isbn, String loanId, 
			String userId, String reason, int schemaVersion) {

		String aggregateId = isbn == null || isbn.isBlank() ? "9788804336327" : isbn;
		
		return new IntegrationEventEnvelope<>(UUID.randomUUID().toString(), IntegrationEventTypes.BOOK_RESERVATION_REJECTED, "test-handler", 
				aggregateId, AggregateType.BOOK.name(), 0, Instant.now(), schemaVersion, 
				new BookReservationRejectedIntegrationPayload(isbn, loanId, userId, reason));
	}
}
