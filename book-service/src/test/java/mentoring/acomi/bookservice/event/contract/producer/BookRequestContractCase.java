package mentoring.acomi.bookservice.event.contract.producer;

import mentoring.acomi.bookservice.domain.events.bookrequest.BookRequestEvent;

public record BookRequestContractCase(String name, String schemaPath, String samplePath, String invalidJson, BookRequestEvent domainEvent) {}
