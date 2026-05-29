package mentoring.acomi.userservice.infrastructure.dto;

public record SuspendRequest(String userId, String reason, String suspendedBy) {}
