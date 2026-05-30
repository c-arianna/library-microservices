package mentoring.acomi.userservice.infrastructure.dto;

public record AuthResponse(String userId, String accessToken, String refreshToken) {}
