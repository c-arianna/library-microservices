package mentoring.acomi.loanservice.infrastructure.persistence.repositories.spec;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.ObjectUtils;

import jakarta.persistence.criteria.Predicate;
import mentoring.acomi.loanservice.application.LoanFilter;
import mentoring.acomi.loanservice.infrastructure.persistence.entity.LoanViewEntity;

public class JpaLoanViewSpecification {
	
	public static Specification<LoanViewEntity> fromFilter(LoanFilter filter) {

		return (root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();

			if (!ObjectUtils.isEmpty(filter.isbn())) {
				predicates.add(cb.equal(root.get("isbn"), filter.isbn()));
			}

			if (!ObjectUtils.isEmpty(filter.userId())) {
				predicates.add(cb.equal(root.get("userId"), filter.userId()));
			}

			if (!ObjectUtils.isEmpty(filter.status())) {
				predicates.add(cb.equal(root.get("status"), filter.status()));
			}

			return cb.and(predicates.toArray(new Predicate[0]));
		};
	}

}
