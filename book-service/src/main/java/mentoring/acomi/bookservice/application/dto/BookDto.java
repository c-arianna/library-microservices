package mentoring.acomi.bookservice.application.dto;

public record BookDto(String isbn, String author, String title, String description, int totalCopies, int borrowedCopies, int reservedCopies, 
		boolean available) {

}