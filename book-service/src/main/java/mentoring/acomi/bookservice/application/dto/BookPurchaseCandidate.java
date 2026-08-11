package mentoring.acomi.bookservice.application.dto;

import java.math.BigDecimal;

public record BookPurchaseCandidate(String requestId, String title, String author, BigDecimal estimatedPrice, int votes, long waitingDays) {}