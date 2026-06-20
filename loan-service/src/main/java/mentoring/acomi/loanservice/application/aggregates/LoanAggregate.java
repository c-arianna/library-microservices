package mentoring.acomi.loanservice.application.aggregates;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import mentoring.acomi.loanservice.domain.errors.InvalidLoanStateTransition;
import mentoring.acomi.loanservice.domain.errors.LoanNotExist;
import mentoring.acomi.loanservice.domain.events.LoanCanceledEvent;
import mentoring.acomi.loanservice.domain.events.LoanConfirmRequestedEvent;
import mentoring.acomi.loanservice.domain.events.LoanConfirmedEvent;
import mentoring.acomi.loanservice.domain.events.LoanEvent;
import mentoring.acomi.loanservice.domain.events.LoanFailedEvent;
import mentoring.acomi.loanservice.domain.events.LoanFailedReason;
import mentoring.acomi.loanservice.domain.events.LoanRequestedEvent;
import mentoring.acomi.loanservice.domain.events.LoanReservedEvent;
import mentoring.acomi.loanservice.domain.events.LoanReturnedEvent;
import mentoring.acomi.loanservice.domain.events.payload.LoanFailedPayload;
import mentoring.acomi.loanservice.domain.events.payload.LoanPayload;
import mentoring.acomi.loanservice.domain.events.payload.LoanRequestPayload;
import mentoring.acomi.loanservice.domain.model.Loan;
import mentoring.acomi.loanservice.domain.model.LoanStatus;

public class LoanAggregate  {

	Map<LoanStatus, List<LoanStatus>> allowedTransitions = Map.of(
			LoanStatus.PENDING, List.of(LoanStatus.RESERVED, LoanStatus.FAILED), 
			LoanStatus.CANCELED, List.of(), 
			LoanStatus.RETURNED, List.of(), 
			LoanStatus.FAILED, List.of(), 
			LoanStatus.CONFIRMED, List.of(LoanStatus.RETURNED),
			LoanStatus.RESERVED, List.of(LoanStatus.CONFIRMED, LoanStatus.CANCELED, LoanStatus.FAILED));

	private String id;
	private boolean isCreated = false;
	private LoanStatus status;
	private String isbn;
	private String userId;
	
	private int version = -1;
	
	private Consumer<LoanEvent> dispatcher;
	
	public LoanAggregate(String id, Consumer<LoanEvent> dispatcher, List<LoanEvent> events) {
		this.id = id;
		this.dispatcher = dispatcher;
		replay(events);
	}

	private void replay(List<LoanEvent> events) {
		for (LoanEvent event : events) {
			apply(event);
		}
	}
	
	public void apply(LoanEvent event) {
		
		int expectedVersion = version + 1;
		if (event.eventVersion() != expectedVersion) {
			throw new IllegalStateException(String.format("Invalid event version, expected %d, actual %d", expectedVersion, event.eventVersion()));
		}
		
		switch (event) {
			case LoanRequestedEvent e -> applyLoanRequestedEvent(e);
			case LoanFailedEvent e -> applyLoanFailedEvent(e);
			case LoanReservedEvent e -> applyLoanReservedEvent(e);
			case LoanConfirmedEvent e -> applyLoanConfirmedEvent(e);
			case LoanCanceledEvent e -> applyLoanCanceledEvent(e);
			case LoanReturnedEvent e -> applyLoanReturnedEvent(e);
			case LoanConfirmRequestedEvent e -> {}
		}

		version++;
	}

	private void applyLoanRequestedEvent(LoanRequestedEvent event) {
		LoanRequestPayload payload = event.payload();
		status = LoanStatus.PENDING;
		isbn = payload.isbn();
		userId = payload.userId();
		isCreated = true;
	}

	private void applyLoanFailedEvent(LoanFailedEvent e) {
		status = LoanStatus.FAILED;
	}

	private void applyLoanReservedEvent(LoanReservedEvent e) {
		status = LoanStatus.RESERVED;
	}

	private void applyLoanConfirmedEvent(LoanConfirmedEvent e) {
		status = LoanStatus.CONFIRMED;
	}

	private void applyLoanCanceledEvent(LoanCanceledEvent e) {
		status = LoanStatus.CANCELED;
	}

	private void applyLoanReturnedEvent(LoanReturnedEvent e) {
		status = LoanStatus.RETURNED;
	}

	public void add(Loan loan) {

		if (this.isCreated) {
			throw new InvalidLoanStateTransition("Loan Already Created");
		}

		LoanRequestPayload payload = new LoanRequestPayload(loan.getId(), loan.getIsbn(), loan.getUserId(),
				loan.getPeriod(), LoanStatus.PENDING);
		LoanRequestedEvent event = new LoanRequestedEvent(loan.getId(), getEventId(), nextVersion(), payload, Instant.now());
		manageEvent(event);
	}

	public void reserve() {

		ensureCreated();

		if (!LoanStatus.RESERVED.equals(status)) {

			ensureTransitionAllowed(LoanStatus.RESERVED);

			LoanReservedEvent event = new LoanReservedEvent(id, getEventId(), nextVersion(), new LoanPayload(id, isbn, userId), Instant.now());
			manageEvent(event);
		}

	}

	public void fail(LoanFailedReason reason) {

		ensureCreated();

		if (!LoanStatus.CANCELED.equals(status) && !LoanStatus.FAILED.equals(status)
				&& !LoanStatus.RETURNED.equals(status)) {

			ensureTransitionAllowed(LoanStatus.FAILED);

			LoanFailedEvent event = new LoanFailedEvent(id, getEventId(), nextVersion(), new LoanFailedPayload(id, reason), Instant.now());
			manageEvent(event);
		}

	}

	public void confirm() {

		ensureCreated();

		if (!LoanStatus.CONFIRMED.equals(status)) {

			ensureTransitionAllowed(LoanStatus.CONFIRMED);

			LoanConfirmedEvent event = new LoanConfirmedEvent(id, getEventId(), nextVersion(), new LoanPayload(id, isbn, userId), Instant.now());
			manageEvent(event);
		}

	}

	public void cancel() {

		ensureCreated();

		if (!LoanStatus.CANCELED.equals(status)) {

			ensureTransitionAllowed(LoanStatus.CANCELED);

			LoanCanceledEvent event = new LoanCanceledEvent(id, getEventId(), nextVersion(), new LoanPayload(id, isbn, userId),
					Instant.now());
			manageEvent(event);
		}

	}

	public void returnLoan() {

		ensureCreated();

		if (!LoanStatus.RETURNED.equals(status)) {

			ensureTransitionAllowed(LoanStatus.RETURNED);

			LoanReturnedEvent event = new LoanReturnedEvent(id, getEventId(), nextVersion(), new LoanPayload(id, isbn, userId), Instant.now());
			manageEvent(event);
		}

	}

	public void requestConfirm() {

		ensureCreated();

		if (LoanStatus.RESERVED.equals(status)) {
			ensureTransitionAllowed(LoanStatus.CONFIRMED);
			LoanConfirmRequestedEvent event = new LoanConfirmRequestedEvent(id, getEventId(), nextVersion(), new LoanPayload(id, isbn, userId), Instant.now());
			manageEvent(event);
		}
	}

	public void ensureCreated() {
		if (!isCreated) {
			throw new LoanNotExist("Loan not exists");
		}
	}

	private void ensureTransitionAllowed(LoanStatus next) {

		List<LoanStatus> transitions = allowedTransitions.get(status);
		if (transitions.isEmpty() || !transitions.contains(next)) {
			throw new InvalidLoanStateTransition(
					String.format("Status transition from: %s, to: %s not allowed", status.name(), next.name()));
		}

	}

	public boolean isConfirmable() {
		return LoanStatus.RESERVED.equals(status);
	}

	public String getIsbn() {
		return isbn;
	}

	public String getUserId() {
		return userId;
	}
	
	private void manageEvent(LoanEvent event) {
		apply(event);
		dispatcher.accept(event);
	}
	
	private String getEventId() {
		return UUID.randomUUID().toString();
	}
	
	private int nextVersion() {
		return version + 1;
	}

}
