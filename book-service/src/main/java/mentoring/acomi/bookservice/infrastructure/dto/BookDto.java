package mentoring.acomi.bookservice.infrastructure.dto;

public record BookDto(String isbn, String author, String title, String description, boolean available) {

}