package mentoring.acomi.bookservice.domain.events.bookrequest;

import mentoring.acomi.bookservice.domain.events.ProducerEventType;

public enum BookRequestEventType implements ProducerEventType {
	
    BookRequestAdded, BookRequestVoted, BookRequestApproved, BookRequestRejected;
    
    @Override
    public String eventName() {
        return name();
    }
}
