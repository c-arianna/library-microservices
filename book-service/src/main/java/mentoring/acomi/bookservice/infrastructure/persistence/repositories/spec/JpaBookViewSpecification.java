package mentoring.acomi.bookservice.infrastructure.persistence.repositories.spec;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.ObjectUtils;

import jakarta.persistence.criteria.Predicate;
import mentoring.acomi.bookservice.application.BookFilter;
import mentoring.acomi.bookservice.infrastructure.persistence.entity.BookViewEntity;

import java.util.ArrayList;
import java.util.List;

public class JpaBookViewSpecification {

	public static Specification<BookViewEntity> fromFilter(BookFilter filter) {

		return (root, query, cb) -> {

			List<Predicate> predicates = new ArrayList<>();

			if (!ObjectUtils.isEmpty(filter.isbn())) {
				predicates.add(cb.equal(root.get("isbn"), filter.isbn()));
			}

			if (!ObjectUtils.isEmpty(filter.author())) {
				predicates.add(cb.equal(root.get("author"), filter.author()));
			}

			if (!ObjectUtils.isEmpty(filter.title())) {
				predicates.add(cb.like(cb.lower(root.get("title")), String.join("", "%", filter.title().toLowerCase(), "%")));
			}

			if (filter.onlyAvailable()) {
				predicates.add(cb.greaterThan(cb.diff(cb.diff(root.get("totalCopies"), root.get("borrowedCopies")),
						root.get("reservedCopies")), 0));

			}

			return cb.and(predicates.toArray(new Predicate[0]));
		};
	}
}