package mentoring.acomi.bookservice.application.dto;

import java.math.BigDecimal;

public record BookToPurchaseDto(String requestId, String title, String author, BigDecimal estimatedPrice, int votes, long waitingDays,
        long score) {}