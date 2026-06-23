package mentoring.acomi.loanservice.application.reactor.command;

public record CommandBookRejectedEvent(String loanId, String reason) {

}
