package mentoring.acomi.userservice.domain.events.payload;

public record UserUnsubscribedPayload(String userId, String email, String reason) {

}
