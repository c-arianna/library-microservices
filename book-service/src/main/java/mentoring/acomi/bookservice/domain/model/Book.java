package mentoring.acomi.bookservice.domain.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
@Builder
public class Book {
    
	private final ISBN isbn;
    private final Author author;
    private final Title title;
    private final Description description;
    
    private Book(ISBN isbn, Author author, Title title, Description description) {
    	this.isbn = isbn;
    	this.author = author;
    	this.title = title;
    	this.description = description;	
    }
    
    public static Book create(String isbn, String author, String title, String description)   {
    	return new Book(ISBN.of(isbn), new Author(author), new Title(title), new Description(description));
    }
    
    public String getIsbn() {
    	return isbn!= null ? isbn.getValue() : "";
    }
    
    public String getAuthor() {
    	return author!= null ? author.getValue(): "";
    }
    
    public String getTitle() {
    	return title!=null ? title.getValue(): "";
    }
    
    public String getDescription() {
    	return description!=null ? description.getValue() : "";
    }
}

