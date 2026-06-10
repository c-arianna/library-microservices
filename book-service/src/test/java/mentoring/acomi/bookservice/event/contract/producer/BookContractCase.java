package mentoring.acomi.bookservice.event.contract.producer;

import mentoring.acomi.bookservice.domain.events.BookEvent;

public record BookContractCase(String name, String schemaPath, String samplePath, String invalidJson, BookEvent domainEvent) {}
