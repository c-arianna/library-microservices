package mentoring.acomi.userservice.infrastructure.persistence.repositories.spec;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.ObjectUtils;

import jakarta.persistence.criteria.Predicate;
import mentoring.acomi.userservice.application.UserFilter;
import mentoring.acomi.userservice.infrastructure.persistence.entity.UserViewEntity;

public class JpaUserViewSpecification {
	
	public static Specification<UserViewEntity> fromFilter(UserFilter filter) {

		return (root, query, cb) -> {

			List<Predicate> predicates = new ArrayList<>();

			if (!ObjectUtils.isEmpty(filter.email())) {
				predicates.add(cb.equal(root.get("mail"), filter.email()));
			}

			if (!ObjectUtils.isEmpty(filter.name())) {
				predicates.add(cb.equal(root.get("name"), filter.name()));
			}
			
			if (!ObjectUtils.isEmpty(filter.lastname())) {
				predicates.add(cb.equal(root.get("lastname"), filter.lastname()));
			}

			if (!ObjectUtils.isEmpty(filter.status())) {
				predicates.add(cb.equal(root.get("status"), filter.status()));
			}

			return cb.and(predicates.toArray(new Predicate[0]));
		};
	}

}
