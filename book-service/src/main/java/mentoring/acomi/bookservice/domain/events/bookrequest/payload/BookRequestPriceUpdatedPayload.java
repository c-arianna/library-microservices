package mentoring.acomi.bookservice.domain.events.bookrequest.payload;

import java.math.BigDecimal;

public record BookRequestPriceUpdatedPayload(String requestId, BigDecimal estimatedPrice) {}