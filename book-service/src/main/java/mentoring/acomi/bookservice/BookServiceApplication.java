package mentoring.acomi.bookservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import mentoring.acomi.sharedcodelibrary.configuration.SharedLibraryCoreConfiguration;

@SpringBootApplication
@Import(SharedLibraryCoreConfiguration.class)
public class BookServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(BookServiceApplication.class, args);
	}

}
