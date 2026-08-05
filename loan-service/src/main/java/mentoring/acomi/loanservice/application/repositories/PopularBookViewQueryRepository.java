package mentoring.acomi.loanservice.application.repositories;

import java.util.List;

import mentoring.acomi.loanservice.application.view.PopularBookView;

public interface PopularBookViewQueryRepository {
    List<PopularBookView> findMostPopularBooks(int limit);
}
