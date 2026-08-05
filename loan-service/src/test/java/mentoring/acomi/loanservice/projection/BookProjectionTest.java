package mentoring.acomi.loanservice.projection;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import mentoring.acomi.loanservice.application.repositories.BookViewRepository;
import mentoring.acomi.loanservice.application.view.BookView;
import mentoring.acomi.loanservice.domain.events.AggregateType;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer.BookRegisteredIntegrationPayload;
import mentoring.acomi.loanservice.infrastructure.projection.BookProjection;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;

@ExtendWith(MockitoExtension.class)
public class BookProjectionTest {

    @Mock
    private BookViewRepository repository;

    private BookProjection projection;

    @BeforeEach
    void setUp() {
        projection = new BookProjection(repository);
    }

    @Test
    void shouldSupportBookRegisteredEvent() {
        Assertions.assertTrue(projection.supports(IntegrationEventTypes.BOOK_REGISTERED));
    }

    @Test
    void shouldNotSupportLoanConfirmedEvent() {
    	Assertions.assertFalse(projection.supports(IntegrationEventTypes.LOAN_CONFIRMED));
    }

    @Test
    void shouldInsertBookWhenBookRegisteredEventArrives() {

        BookRegisteredIntegrationPayload payload = new BookRegisteredIntegrationPayload("9788804336327", "Italo Calvino",
                        "Il visconte dimezzato", "");

        IntegrationEventEnvelope<?> event = new IntegrationEventEnvelope<>(UUID.randomUUID().toString(),
                        IntegrationEventTypes.BOOK_REGISTERED, "test", payload.isbn(), AggregateType.BOOK.name(),
                        1, Instant.now(), 1, payload);

        projection.project(event);

        ArgumentCaptor<BookView> captor = ArgumentCaptor.forClass(BookView.class);

        verify(repository).insert(captor.capture());

        BookView inserted = captor.getValue();

        Assertions.assertEquals(payload.isbn(), inserted.isbn());
        Assertions.assertEquals(payload.author(), inserted.author());
        Assertions.assertEquals(payload.title(), inserted.title());

        verifyNoMoreInteractions(repository);
    }
}