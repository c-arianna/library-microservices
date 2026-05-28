package mentoring.acomi.bookservice.domain.events;

public sealed interface BookStateEvent extends BookEvent permits BookRegisteredEvent, BookCopiesAddedEvent, BookCopiesRemovedEvent,
BookReservedEvent, BookBorrowedEvent, BookReleasedEvent, BookReturnedEvent {}
