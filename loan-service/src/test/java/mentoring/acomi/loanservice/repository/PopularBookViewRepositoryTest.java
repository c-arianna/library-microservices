package mentoring.acomi.loanservice.repository;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import mentoring.acomi.loanservice.application.repositories.PopularBookViewRepository;
import mentoring.acomi.loanservice.application.view.PopularBookView;
import mentoring.acomi.loanservice.config.SecurityTestConfig;

@SpringBootTest
@Transactional
@Import(SecurityTestConfig.class)
public class PopularBookViewRepositoryTest {

	@Autowired
	private PopularBookViewRepository repository;
	
	@Test
	void shouldIncrementLoanCountWhenBookAlreadyExists() {
	    
	    PopularBookView book = new PopularBookView("9788804336327", "Italo Calvino", "Il visconte dimezzato", 1);

	    repository.registerLoanCount(book);

	    repository.registerLoanCount(book);

	    PopularBookView result = repository.findByIsbn("9788804336327").orElseThrow();

	    Assertions.assertEquals(2, result.loanCount());
	}
}
