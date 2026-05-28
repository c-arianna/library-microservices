package mentoring.acomi.loanservice.infrastructure.errors.dto;

import java.time.Instant;

public record ErrorResponse(String code, String message, String type, String service, Instant timestamp) {

}
