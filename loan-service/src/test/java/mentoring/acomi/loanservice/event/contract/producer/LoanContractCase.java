package mentoring.acomi.loanservice.event.contract.producer;

import mentoring.acomi.loanservice.domain.events.LoanEvent;

public record LoanContractCase(String name, String schemaPath, String samplePath, String invalidJson, LoanEvent domainEvent) {}
