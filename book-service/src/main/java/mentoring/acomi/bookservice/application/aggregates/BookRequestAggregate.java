package mentoring.acomi.bookservice.application.aggregates;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import mentoring.acomi.bookservice.domain.bookrequest.model.BookRequest;
import mentoring.acomi.bookservice.domain.bookrequest.model.BookRequestStatus;
import mentoring.acomi.bookservice.domain.errors.BookRequestNotExist;
import mentoring.acomi.bookservice.domain.errors.UserAlreadyVoted;
import mentoring.acomi.bookservice.domain.errors.UserRequesterCannotVote;
import mentoring.acomi.bookservice.domain.errors.BookRequestAlreadyClosed;
import mentoring.acomi.bookservice.domain.events.bookrequest.BookRequestAddedEvent;
import mentoring.acomi.bookservice.domain.events.bookrequest.BookRequestApprovedEvent;
import mentoring.acomi.bookservice.domain.events.bookrequest.BookRequestEvent;
import mentoring.acomi.bookservice.domain.events.bookrequest.BookRequestRejectedEvent;
import mentoring.acomi.bookservice.domain.events.bookrequest.BookRequestVotedEvent;
import mentoring.acomi.bookservice.domain.events.bookrequest.payload.BookRequestAddedPayload;
import mentoring.acomi.bookservice.domain.events.bookrequest.payload.BookRequestApprovedPayload;
import mentoring.acomi.bookservice.domain.events.bookrequest.payload.BookRequestRejectedPayload;
import mentoring.acomi.bookservice.domain.events.bookrequest.payload.BookRequestVotedPayload;

public class BookRequestAggregate {
	
	private Consumer<BookRequestEvent> dispatcher;
	
	private String requestId;
	private boolean isCreated = false;
	private BookRequestStatus status;
	private String requesterUserId;
	Set<String> voters = new HashSet<>();
	
	private int version = -1;
	
	public BookRequestAggregate(String requestId, Consumer<BookRequestEvent> dispatcher, List<BookRequestEvent> events) {
		this.requestId = requestId;
		this.dispatcher = dispatcher;
		replay(events);
	}
	
	private void replay(List<BookRequestEvent> events) {
		for (BookRequestEvent event : events) {
			apply(event);
		}
	}

	public void apply(BookRequestEvent event) {

		int expectedVersion = version + 1;
		if (event.eventVersion() != expectedVersion) {
			throw new IllegalStateException(String.format("Invalid event version, expected %d, actual %d", expectedVersion, event.eventVersion()));
		}

		switch (event) {
			case BookRequestAddedEvent e -> applyBookRequestAdded(e);
			case BookRequestVotedEvent e -> applyBookRequestVoted(e);
			case BookRequestApprovedEvent e -> applyBookRequestApproved(e);
			case BookRequestRejectedEvent e -> applyBookRequestRejected(e);
		}

		version++;

	}

	private void applyBookRequestAdded(BookRequestAddedEvent e) {
		requesterUserId = e.payload().requesterUserId();
		status = BookRequestStatus.PENDING;
		isCreated = true;
		voters.add(requesterUserId);
	}
	
	private void applyBookRequestVoted(BookRequestVotedEvent e) {
		voters.add(e.payload().userId());
	}
	
	private void applyBookRequestApproved(BookRequestApprovedEvent e) {
		status = BookRequestStatus.APPROVED;
	}

	private void applyBookRequestRejected(BookRequestRejectedEvent e) {
		status = BookRequestStatus.REJECTED;
	}
	
	public void add(BookRequest bookRequest) {
		
		if(!isCreated) {
			BookRequestAddedPayload payload = new BookRequestAddedPayload(bookRequest.requestId(), bookRequest.author(), bookRequest.title(),
					bookRequest.requesterUserId(), bookRequest.isbn(), bookRequest.notes());
			BookRequestAddedEvent event = new BookRequestAddedEvent(bookRequest.requestId(), getEventId(), nextVersion(), payload,
					Instant.now());
			manageEvent(event);
		}
	}
	
	public void vote(String userId) {
		
		ensureCreated();
		
		if (status != BookRequestStatus.PENDING) {
			throw new BookRequestAlreadyClosed("Request is already close");
		}
		
		if(requesterUserId.equals(userId)) {
			throw new UserRequesterCannotVote("User requester cannot vote");
		}
		
		if (voters.contains(userId)) {
		    throw new UserAlreadyVoted("User already voted");
		}
		
		BookRequestVotedPayload payload = new BookRequestVotedPayload(requestId, userId);
		BookRequestVotedEvent event = new BookRequestVotedEvent(requestId, getEventId(), nextVersion(), payload, Instant.now());
		manageEvent(event);
		
	}
	
	public void approve() {
		
		ensureCreated();
		
		if(status ==  BookRequestStatus.PENDING) {
			BookRequestApprovedPayload payload = new BookRequestApprovedPayload(requestId);
			BookRequestApprovedEvent event = new BookRequestApprovedEvent(requestId, getEventId(), nextVersion(), payload, Instant.now());
			manageEvent(event);
		}
	}
	
	public void reject(String reason) {
		
		ensureCreated();
		
		if(status ==  BookRequestStatus.PENDING) {
			BookRequestRejectedPayload payload = new BookRequestRejectedPayload(requestId, reason);
			BookRequestRejectedEvent event = new BookRequestRejectedEvent(requestId, getEventId(), nextVersion(), payload, Instant.now());
			manageEvent(event);
		}
		
	}
	
	public void ensureCreated() {
		if (!isCreated) {
			throw new BookRequestNotExist("Book request not exist, ID: %s".formatted(requestId));
		}

	}
	
	private void manageEvent(BookRequestEvent event) {
		apply(event);
		dispatcher.accept(event);
	}

	private String getEventId() {
		return UUID.randomUUID().toString();
	}

	private int nextVersion() {
		return version + 1;
	}
	
	public boolean isPending() {
		return BookRequestStatus.PENDING == status;
		
	}

}
