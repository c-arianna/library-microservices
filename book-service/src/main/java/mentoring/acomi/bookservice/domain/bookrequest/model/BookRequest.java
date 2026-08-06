package mentoring.acomi.bookservice.domain.bookrequest.model;

import mentoring.acomi.bookservice.domain.book.model.Author;
import mentoring.acomi.bookservice.domain.book.model.ISBN;
import mentoring.acomi.bookservice.domain.book.model.Title;

public class BookRequest {

	private final String requestId;
	private final Author author;
	private final Title title;
	private final ISBN isbn;
	private final String requesterUserId;
	private final BookRequestStatus status;
	private final String notes;

	private BookRequest(String requestId, Author author, Title title, ISBN isbn, String requesterUserId, BookRequestStatus status, String notes) {
		this.requestId = requestId;
		this.author = author;
		this.title = title;
		this.isbn = isbn;
		this.requesterUserId = requesterUserId;
		this.status = status;
		this.notes = notes;
	}
	
	public static BookRequest create(String requestId, String author, String title, String isbn, String requesterUserId, String notes) {
		validateRequestId(requestId);
		validateRequesterUserId(requesterUserId);
		return new BookRequest(requestId, new Author(author),  new Title(title), toIsbn(isbn), requesterUserId, BookRequestStatus.PENDING, notes);
	}

	private static ISBN toIsbn(String isbn) {
		return isbn == null || isbn.isBlank() ? null : ISBN.of(isbn);
	}
	
	private static void validateRequestId(String requestId) {

        if (requestId == null || requestId.isBlank()) {
            throw new IllegalArgumentException("requestId cannot be blank");
        }
    }

    private static void validateRequesterUserId(String requesterUserId) {

        if (requesterUserId == null || requesterUserId.isBlank()) {

            throw new IllegalArgumentException("requesterUserId cannot be blank");
        }
    }

    public String requestId() {
        return requestId;
    }

    public String requesterUserId() {
        return requesterUserId;
    }

    public String author() {
    	return author!= null ? author.getValue(): "";
    }

    public String title() {
    	return title!=null ? title.getValue(): "";
    }

    public String isbn() {
    	return isbn!= null ? isbn.getValue() : "";
    }

    public String notes() {
        return notes;
    }

    public BookRequestStatus status() {
        return status;
    }

}
