package mentoring.acomi.userservice.repository;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import mentoring.acomi.userservice.application.repositories.UserViewRepository;
import mentoring.acomi.userservice.application.view.UserView;
import mentoring.acomi.userservice.domain.model.Password;
import mentoring.acomi.userservice.domain.model.UserRole;
import mentoring.acomi.userservice.domain.model.UserStatus;
import mentoring.acomi.userservice.infrastructure.persistence.entity.UserViewEntity;
import mentoring.acomi.userservice.infrastructure.persistence.repositories.UserViewJpaRepository;

@SpringBootTest
@Transactional
public class UserViewRepositoryTest {

	@Autowired
	private UserViewRepository repository;

	@Autowired
	private UserViewJpaRepository jpaRepository;

	@Autowired
	private EntityManager entityManager;


	@Test
	void shouldSaveBookView() {

		String userId = UUID.randomUUID().toString();
		Optional<UserView> userView = repository.findById(userId);
		Assertions.assertTrue(userView.isEmpty());

		UserView user = new UserView(userId, "test@gmail.com", "Arianna", "Comi", "12345678", UserStatus.ACTIVE, UserRole.LIBRARIAN);
		repository.add(user);
		userView = repository.findById(userId);

		Assertions.assertTrue(userView.isPresent());

		UserView userViewFound = userView.get();

		String hashedPassword = Password.fromHash("12345678").value();
		
		Assertions.assertAll(() -> Assertions.assertEquals(userId, userViewFound.id()),
				() -> Assertions.assertEquals("test@gmail.com", userViewFound.email()),
				() -> Assertions.assertEquals("Arianna", userViewFound.name()),
				() -> Assertions.assertEquals("Comi", userViewFound.lastname()),
				() -> Assertions.assertEquals(hashedPassword, userViewFound.password()),
				() -> Assertions.assertEquals(UserStatus.ACTIVE, userViewFound.status()),
				() -> Assertions.assertEquals(UserRole.LIBRARIAN, userViewFound.role()));

	}

	@Test
	void reserveShouldIncrementReservedCopies() {

		String userId = UUID.randomUUID().toString();
		createUser(userId, "test@gmail.com");

		entityManager.clear();
		
		Optional<UserView> userViewFound = repository.findByEmail("test@gmail.com");
		
		Assertions.assertTrue(userViewFound.isPresent());

	}

	private void createUser(String userId, String email) {
		UserViewEntity entity = new UserViewEntity(userId, email,"Arianna", "Comi", "12345678", UserStatus.ACTIVE, UserRole.READER);
		jpaRepository.saveAndFlush(entity);
	}

}