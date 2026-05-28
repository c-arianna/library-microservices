package mentoring.acomi.loanservice.domain.events;

public sealed interface LoanProcessEvent extends LoanEvent permits LoanConfirmRequestedEvent{

}
