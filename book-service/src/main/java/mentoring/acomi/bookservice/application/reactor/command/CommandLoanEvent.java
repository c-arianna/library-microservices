package mentoring.acomi.bookservice.application.reactor.command;

public record CommandLoanEvent(String loanId, String isbn, String userId) {}
