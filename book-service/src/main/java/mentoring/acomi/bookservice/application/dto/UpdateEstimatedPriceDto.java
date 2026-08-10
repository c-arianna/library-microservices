package mentoring.acomi.bookservice.application.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;

public record UpdateEstimatedPriceDto(@NotNull BigDecimal estimatedPrice) {}