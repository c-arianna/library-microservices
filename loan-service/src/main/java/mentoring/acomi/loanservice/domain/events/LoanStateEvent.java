package mentoring.acomi.loanservice.domain.events;

public sealed interface LoanStateEvent extends LoanEvent permits LoanRequestedEvent, LoanFailedEvent, LoanReservedEvent, LoanConfirmedEvent, LoanCanceledEvent, 
LoanReturnedEvent{}
