package mentoring.acomi.bookservice.application.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import mentoring.acomi.bookservice.application.dto.BookToPurchaseDto;
import mentoring.acomi.bookservice.application.dto.BookPurchaseCandidate;
import mentoring.acomi.bookservice.application.dto.BookPurchaseSuggestionDto;
import mentoring.acomi.bookservice.application.dto.BookPurchaseSuggestionStatus;
import mentoring.acomi.bookservice.application.purchasesuggestion.services.BookPurchaseSuggestionService;
import mentoring.acomi.bookservice.application.repositories.BookRequestViewQueryRepository;
import mentoring.acomi.bookservice.application.view.BookRequestView;
import mentoring.acomi.bookservice.domain.bookrequest.model.BookRequestStatus;

@Service
public class KnapsackBookPurchaseSuggestionService implements BookPurchaseSuggestionService {

    private final BookRequestViewQueryRepository repository;
    
    public KnapsackBookPurchaseSuggestionService(BookRequestViewQueryRepository repository) {
    	this.repository = repository;
    }

    @Override
    public BookPurchaseSuggestionDto suggest(BigDecimal budget) {
    	
    	long pendingRequests = repository.countByStatus(BookRequestStatus.PENDING);
    	 
    	if (pendingRequests == 0) {
            return new BookPurchaseSuggestionDto(BookPurchaseSuggestionStatus.NO_PENDING_REQUESTS, budget, BigDecimal.ZERO, 0, List.of());
        }
    	
        List<BookPurchaseCandidate> candidates = repository.findPurchasableRequests().stream().map(this::toCandidate).toList();

        if (candidates.isEmpty()) {
        	 return new BookPurchaseSuggestionDto(BookPurchaseSuggestionStatus.NO_REQUESTS_WITH_PRICE, budget, BigDecimal.ZERO, 0, List.of());
        }
        
        List<BookPurchaseCandidate> selected = selectBooks(candidates, budget);

        if(selected.isEmpty()) {
        	  return new BookPurchaseSuggestionDto(BookPurchaseSuggestionStatus.BUDGET_TOO_LOW, budget, BigDecimal.ZERO, 0, List.of());
        }
        
        BigDecimal totalCost = selected.stream().map(BookPurchaseCandidate::estimatedPrice).reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalScore = selected.stream().mapToLong(this::score).sum();

        return new BookPurchaseSuggestionDto(BookPurchaseSuggestionStatus.SUCCESS, budget, totalCost, totalScore, 
        		selected.stream().map(this::toBookToPurchaseDto).toList());
    }

    private BookPurchaseCandidate toCandidate(BookRequestView view) {
    	long waitingDays = ChronoUnit.DAYS.between(view.createdAt().atZone(ZoneId.systemDefault()).toLocalDate(), LocalDate.now());
        return new BookPurchaseCandidate(view.requestId(), view.title(), view.author(), view.estimatedPrice(), view.votes(), waitingDays);
    }

    public long score(BookPurchaseCandidate candidate) {
        return (long) candidate.votes() * candidate.votes() + candidate.waitingDays();
    }

    public List<BookPurchaseCandidate> selectBooks(List<BookPurchaseCandidate> candidates, BigDecimal budget) {

        int n = candidates.size();

        int budgetUnits = budget.setScale(0, RoundingMode.HALF_UP).intValueExact();

        long[][] optimalScores = getOptimalScores(candidates, n, budgetUnits);

        return getSelectedBooks(candidates, optimalScores, budgetUnits);
    }

	private long[][] getOptimalScores(List<BookPurchaseCandidate> candidates, int n, int budgetCents) {
		
		long[][] optimalScores = new long[n + 1][budgetCents + 1];

        for (int i = 1; i <= n; i++) {

            BookPurchaseCandidate candidate = candidates.get(i - 1);

            int cost = candidate.estimatedPrice().setScale(0, RoundingMode.HALF_UP).intValueExact();

            long value = score(candidate);

            for (int currentBudget = 0; currentBudget <= budgetCents; currentBudget++) {
            	 optimalScores[i][currentBudget] = cost > currentBudget ? optimalScores[i - 1][currentBudget] : 
            		 Math.max(optimalScores[i - 1][currentBudget],  optimalScores[i - 1][currentBudget - cost] + value);
            }
        }
        
		return optimalScores;
	}

    private List<BookPurchaseCandidate> getSelectedBooks(List<BookPurchaseCandidate> candidates, long[][] dp, int budgetCents) {

        List<BookPurchaseCandidate> selectedBooks = new ArrayList<>();

        int budget = budgetCents;

        for (int i = candidates.size(); i > 0; i--) {

            if (dp[i][budget] != dp[i - 1][budget]) {
                BookPurchaseCandidate candidate = candidates.get(i - 1);
                selectedBooks.add(candidate);
                budget -= candidate.estimatedPrice().setScale(0, RoundingMode.HALF_UP).intValueExact();
            }
        }

        Collections.reverse(selectedBooks);

        return selectedBooks;
    }

    private BookToPurchaseDto toBookToPurchaseDto(BookPurchaseCandidate candidate) {

        return new BookToPurchaseDto(candidate.requestId(), candidate.title(), candidate.author(), candidate.estimatedPrice(),
                candidate.votes(), candidate.waitingDays(), score(candidate));
    }
}