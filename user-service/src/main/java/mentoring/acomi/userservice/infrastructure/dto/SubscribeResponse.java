package mentoring.acomi.userservice.infrastructure.dto;

public record SubscribeResponse(String userId, String email, String accessToken, String refreshToken) {}
