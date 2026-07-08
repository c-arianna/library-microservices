package mentoring.acomi.bookservice.replay;

import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import mentoring.acomi.bookservice.application.repositories.BookViewQueryRepository;
import mentoring.acomi.bookservice.application.services.BookService;
import mentoring.acomi.bookservice.config.RabbitMQConfigTest;
import mentoring.acomi.bookservice.config.SecurityTestConfig;
import mentoring.acomi.bookservice.infrastructure.dto.AddBookCopiesRequest;
import mentoring.acomi.bookservice.infrastructure.dto.AddBookRequest;
import mentoring.acomi.bookservice.infrastructure.dto.RemoveBookCopiesRequest;
import mentoring.acomi.bookservice.infrastructure.messaging.replay.BookReplayService;
import mentoring.acomi.bookservice.infrastructure.persistence.entity.BookViewEntity;
import mentoring.acomi.bookservice.infrastructure.persistence.repositories.BookViewJpaRepository;

@SpringBootTest
@Testcontainers
@ActiveProfiles("H2")
@Import({ RabbitMQConfigTest.class, SecurityTestConfig.class })
public class BookEventsReplayTest {

	@Container
	private static RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:3-management");

	@DynamicPropertySource
	static void rabbitProps(DynamicPropertyRegistry registry) {
		registry.add("spring.rabbitmq.host", rabbit::getHost);
		registry.add("spring.rabbitmq.port", rabbit::getAmqpPort);
		registry.add("spring.rabbitmq.username", rabbit::getAdminUsername);
		registry.add("spring.rabbitmq.password", rabbit::getAdminPassword);
	}

	@Autowired
	private RabbitListenerEndpointRegistry registry;
	
	@Autowired
	private BookService bookService;

	@Autowired
	private BookViewJpaRepository viewRepository;

	@Autowired
	private BookViewQueryRepository bookViewRepository;

	@Autowired
	private BookReplayService replayService;

	private static final String ISBN = "9788804336327";

	@AfterEach
	void stopListeners() {
	    registry.stop();
	}
	
	@Test
	void shouldRebuildProjectionsFromEventsReplay() {

		bookService.addBook(new AddBookRequest(ISBN, "Italo Calvino", "Il barone rampante", ""));
		bookService.addBookCopies(new AddBookCopiesRequest(3), ISBN);
		bookService.removeBookCopies(new RemoveBookCopiesRequest(1, ""), ISBN);

		await().atMost(Duration.ofSeconds(50)).untilAsserted(() -> {
			var book = bookViewRepository.findById(ISBN);
			Assertions.assertThat(book.isPresent()).isTrue();
			Assertions.assertThat(2).isEqualTo(book.get().availableCopies());
		});

		List<BookViewEntity> expectedBooks = viewRepository.findAll();

		Assertions.assertThat(!expectedBooks.isEmpty());
		
		replayService.rebuild();

		List<BookViewEntity> actualBooks = viewRepository.findAll();

		Assertions.assertThat(actualBooks).usingRecursiveComparison().isEqualTo(expectedBooks);

	}

}
