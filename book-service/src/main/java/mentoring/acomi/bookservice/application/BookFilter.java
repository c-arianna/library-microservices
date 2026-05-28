package mentoring.acomi.bookservice.application;

public record BookFilter(String title, String author, String isbn, boolean onlyAvailable) {

}
