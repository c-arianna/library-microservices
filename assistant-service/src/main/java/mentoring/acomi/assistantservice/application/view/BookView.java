package mentoring.acomi.assistantservice.application.view;

public record BookView(String isbn, String author, String title, String description, int totalCopies, int borrowedCopies, 
		int reservedCopies) implements ProjectionView<String>{
	
	 @Override
	 public String id() {
		 return isbn();
	 }
	 
	 public boolean available() {
		 return totalCopies - borrowedCopies - reservedCopies > 0;
	 }
}
