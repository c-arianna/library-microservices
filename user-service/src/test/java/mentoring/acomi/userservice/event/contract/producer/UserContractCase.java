package mentoring.acomi.userservice.event.contract.producer;

import mentoring.acomi.userservice.domain.events.UserEvent;

public record UserContractCase(String name, String schemaPath, String samplePath, String invalidJson, UserEvent domainEvent) {}
