package mentoring.acomi.sharedcorelibrary.model;

import java.time.Instant;

public record ErrorResponse(String code, String message, String type, String service, Instant timestamp) {

}
