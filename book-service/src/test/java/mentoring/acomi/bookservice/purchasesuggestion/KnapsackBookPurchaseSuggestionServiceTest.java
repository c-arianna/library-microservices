package mentoring.acomi.bookservice.purchasesuggestion;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import mentoring.acomi.bookservice.application.dto.BookPurchaseCandidate;
import mentoring.acomi.bookservice.application.repositories.BookRequestViewQueryRepository;
import mentoring.acomi.bookservice.application.services.KnapsackBookPurchaseSuggestionService;

@ExtendWith(MockitoExtension.class)
public class KnapsackBookPurchaseSuggestionServiceTest {

	@Mock
	private BookRequestViewQueryRepository repository;
	
    private KnapsackBookPurchaseSuggestionService service;
    
    @BeforeEach
    void setUp() {
    	service = new KnapsackBookPurchaseSuggestionService(repository);
    }

    @Test
    void shouldReturnEmptyListWhenNoCandidatesExist() {
        List<BookPurchaseCandidate> selected = service.selectBooks(List.of(), BigDecimal.valueOf(100));
        assertThat(selected).isEmpty();
    }

    @Test
    void shouldReturnEmptyListWhenBudgetIsZero() {
        List<BookPurchaseCandidate> candidates = List.of(candidate("A", 20, 1, 0), candidate("B", 30, 1, 0));
        List<BookPurchaseCandidate> selected = service.selectBooks(candidates, BigDecimal.ZERO);
        assertThat(selected).isEmpty();
    }

    @Test
    void shouldReturnEmptyListWhenNoBookFitsBudget() {
        List<BookPurchaseCandidate> candidates = List.of(candidate("A", 100, 1, 0), candidate("B", 200, 1, 0));
        List<BookPurchaseCandidate> selected = service.selectBooks(candidates, BigDecimal.valueOf(50));
        assertThat(selected).isEmpty();
    }

    @Test
    void shouldSelectSingleBookWhenOnlyOneFitsBudget() {
        List<BookPurchaseCandidate> candidates = List.of(candidate("A", 20, 1, 0), candidate("B", 50, 1, 0));
        List<BookPurchaseCandidate> selected = service.selectBooks(candidates, BigDecimal.valueOf(25));
        assertThat(selected).extracting(BookPurchaseCandidate::requestId).containsExactly("A");
    }

    @Test
    void shouldSelectAllBooksWhenBudgetAllows() {
        List<BookPurchaseCandidate> candidates = List.of(candidate("A", 20, 3, 0), candidate("B", 30, 1, 0), candidate("C", 10, 2, 0));
        List<BookPurchaseCandidate> selected = service.selectBooks(candidates, BigDecimal.valueOf(100));
        assertThat(selected).extracting(BookPurchaseCandidate::requestId).containsExactly("A", "B", "C");
    }

    @Test
    void shouldChooseOptimalCombination() {
        List<BookPurchaseCandidate> candidates = List.of(candidate("A", 20, 2, 0), candidate("B", 30, 4, 0), candidate("C", 50, 2, 0));
        List<BookPurchaseCandidate> selected = service.selectBooks(candidates, BigDecimal.valueOf(50));
        assertThat(selected).extracting(BookPurchaseCandidate::requestId).containsExactlyInAnyOrder("A", "B");
    }

    @Test
    void shouldPreferCombinationWithHigherScore() {
        List<BookPurchaseCandidate> candidates = List.of(candidate("A", 40, 4, 0), candidate("B", 20, 3, 0), candidate("C", 20, 4, 0));
        List<BookPurchaseCandidate> selected = service.selectBooks(candidates, BigDecimal.valueOf(40));
        assertThat(selected).extracting(BookPurchaseCandidate::requestId).containsExactlyInAnyOrder("B", "C");
    }

    @Test
    void shouldCalculateMaximumPossibleScore() {
        List<BookPurchaseCandidate> candidates = List.of(candidate("A", 20, 10, 0), candidate("B", 30, 5, 0), candidate("C", 50, 11, 0));
        List<BookPurchaseCandidate> selected = service.selectBooks(candidates, BigDecimal.valueOf(50));

        long totalScore = selected.stream().mapToLong(book -> service.score(book)).sum();

        assertThat(totalScore).isEqualTo(125);
    }

    @Test
    void shouldSelectBookWhenCostEqualsBudget() {
        List<BookPurchaseCandidate> candidates = List.of(candidate("A", 50, 1, 0));
        List<BookPurchaseCandidate> selected = service.selectBooks(candidates, BigDecimal.valueOf(50));
        assertThat(selected).extracting(BookPurchaseCandidate::requestId).containsExactly("A");
    }

    @Test
    void shouldChooseOlderRequestWhenVotesAreEqual() {
        BookPurchaseCandidate first = new BookPurchaseCandidate("A", "Title A", "Author A", BigDecimal.valueOf(50), 2, 1000);
        BookPurchaseCandidate second = new BookPurchaseCandidate("B", "Title B", "Author B", BigDecimal.valueOf(50), 2, 200);
        List<BookPurchaseCandidate> selected = service.selectBooks(List.of(first, second), BigDecimal.valueOf(50));

        assertThat(selected).hasSize(1);
    }

    private BookPurchaseCandidate candidate(String requestId, int price, int votes, long waitingDays) {
        return new BookPurchaseCandidate(requestId, "Title", "Author", BigDecimal.valueOf(price), votes, waitingDays);
    }
}