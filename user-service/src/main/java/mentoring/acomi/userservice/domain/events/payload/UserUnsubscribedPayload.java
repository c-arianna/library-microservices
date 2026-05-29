package mentoring.acomi.userservice.domain.events.payload;

public record UserUnsubscribedPayload(String id, String email, String reason) {

}
