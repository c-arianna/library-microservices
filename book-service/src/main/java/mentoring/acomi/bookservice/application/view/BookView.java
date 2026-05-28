package mentoring.acomi.bookservice.application.view;

public record BookView(
	String isbn,
	String author,
	String title,
	String description,
	int totalCopies,
	int availableCopies,
	int borrowedCopies,
	int reservedCopies
) {}
