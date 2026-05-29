package mentoring.acomi.userservice.domain.events.payload;

public record UserPayload(String userId, String email, String reason, String suspendedBy) {

}
