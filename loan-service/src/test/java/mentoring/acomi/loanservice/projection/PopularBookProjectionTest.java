package mentoring.acomi.loanservice.projection;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import mentoring.acomi.loanservice.application.repositories.BookViewRepository;
import mentoring.acomi.loanservice.application.repositories.PopularBookViewRepository;
import mentoring.acomi.loanservice.application.view.BookView;
import mentoring.acomi.loanservice.application.view.PopularBookView;
import mentoring.acomi.loanservice.domain.events.AggregateType;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.producer.LoanIntegrationPayload;
import mentoring.acomi.loanservice.infrastructure.projection.PopularBookProjection;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;

@ExtendWith(MockitoExtension.class)
class PopularBookProjectionTest {

    @Mock
    private BookViewRepository bookRepository;

    @Mock
    private PopularBookViewRepository repository;

    private PopularBookProjection projection;

    @BeforeEach
    void setUp() {
        projection = new PopularBookProjection(bookRepository, repository);
    }

    @Test
    void shouldSupportLoanConfirmedEvent() {
    	Assertions.assertTrue(projection.supports(IntegrationEventTypes.LOAN_CONFIRMED));
    }

    @Test
    void shouldNotSupportBookRegisteredEvent() {
    	Assertions.assertFalse(projection.supports(IntegrationEventTypes.BOOK_REGISTERED));
    }

    @Test
    void shouldRegisterLoanCount() {

        String isbn = "9788804336327";

        BookView book = new BookView(isbn, "Italo Calvino", "Il visconte dimezzato");

        when(bookRepository.findByIsbn(isbn)).thenReturn(Optional.of(book));

        LoanIntegrationPayload payload = new LoanIntegrationPayload("loan-1", isbn, "user-1");

        IntegrationEventEnvelope<?> event = new IntegrationEventEnvelope<>(UUID.randomUUID().toString(),
                        IntegrationEventTypes.LOAN_CONFIRMED, "test", "loan-1", AggregateType.LOAN.name(), 1, Instant.now(),
                        1, payload);

        projection.project(event);

        ArgumentCaptor<PopularBookView> captor = ArgumentCaptor.forClass(PopularBookView.class);

        verify(repository).registerLoanCount(captor.capture());

        PopularBookView popularBook = captor.getValue();

        Assertions.assertEquals(isbn, popularBook.isbn());

        Assertions.assertEquals("Italo Calvino", popularBook.author());

        Assertions.assertEquals("Il visconte dimezzato", popularBook.title());

        Assertions.assertEquals(1, popularBook.loanCount());

        verify(bookRepository).findByIsbn(isbn);
    }

    @Test
    void shouldThrowExceptionWhenBookIsNotFound() {

        String isbn = "9788804336327";

        when(bookRepository.findByIsbn(isbn)).thenReturn(Optional.empty());

        LoanIntegrationPayload payload = new LoanIntegrationPayload("loan-1", isbn, "user-1");

        IntegrationEventEnvelope<?> event = new IntegrationEventEnvelope<>(UUID.randomUUID().toString(),
                        IntegrationEventTypes.LOAN_CONFIRMED, "test", "loan-1", AggregateType.LOAN.name(), 1,
                        Instant.now(), 1, payload);

        Assertions.assertThrows(NoSuchElementException.class, () -> projection.project(event));
    }
}