package mentoring.acomi.loanservice.application.repositories;

import java.util.Optional;

import mentoring.acomi.loanservice.application.view.PopularBookView;

public interface PopularBookViewRepository {
	public void registerLoanCount(PopularBookView view);
	void deleteAll();
	public Optional<PopularBookView> findByIsbn(String string);
}
