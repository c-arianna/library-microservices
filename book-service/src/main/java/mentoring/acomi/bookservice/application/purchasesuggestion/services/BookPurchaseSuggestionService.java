package mentoring.acomi.bookservice.application.purchasesuggestion.services;

import java.math.BigDecimal;

import mentoring.acomi.bookservice.application.dto.BookPurchaseSuggestionDto;

public interface BookPurchaseSuggestionService {
    BookPurchaseSuggestionDto suggest(BigDecimal budget);
}
