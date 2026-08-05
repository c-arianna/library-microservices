package mentoring.acomi.loanservice.application.dto;

public record PopularBookDto(String isbn, String author, String title, int loanCount) {}

