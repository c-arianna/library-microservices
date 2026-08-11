package mentoring.acomi.bookservice.application.dto;

import java.math.BigDecimal;
import java.util.List;

public record BookPurchaseSuggestionDto(BookPurchaseSuggestionStatus status, BigDecimal budget, BigDecimal totalCost, long totalScore, List<BookToPurchaseDto> books) {}