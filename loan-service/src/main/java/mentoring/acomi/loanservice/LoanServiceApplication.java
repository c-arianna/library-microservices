package mentoring.acomi.loanservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import mentoring.acomi.sharedcodelibrary.configuration.SharedLibraryCoreConfiguration;

@SpringBootApplication
@Import(SharedLibraryCoreConfiguration.class)
public class LoanServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(LoanServiceApplication.class, args);
	}

}
