package mentoring.acomi.loanservice.event.contract.consumer;

import java.io.InputStream;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;

import mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer.BookBorrowRejectedIntegrationPayload;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer.BookLoanIntegrationPayload;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer.BookReservationRejectedIntegrationPayload;
import mentoring.acomi.sharedlibrary.integration.messaging.IntegrationEventEnvelope;

public class LoanEventConsumerContractTest {

	private static final String BOOK_SCHEMA_PATH = "contracts/book/%s/v1/event.schema.json";
	private static final String BOOK_SAMPLE_PATH = "contracts/book/%s/v1/sample.json";

	private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule())
			.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

	@ParameterizedTest(name = "[{index}] valid sample -> {0}")
	@MethodSource("sampleCases")
	void sampleShouldBeValid(String name, BookContractCase testCase) throws Exception {
		validateSample(testCase.samplePath(), testCase.schemaPath());
	}

	@Test
	void shouldDeserializeBookBorrowedEvent() throws Exception {
		JsonNode json = objectMapper.readTree(loadResource(String.format(BOOK_SAMPLE_PATH, "book.borrowed")));

		IntegrationEventEnvelope<BookLoanIntegrationPayload> event = objectMapper.treeToValue(json, new TypeReference<>() {});

		Assertions.assertEquals("BOOK_BORROWED", event.eventType().toString());
	}

	@Test
	void shouldDeserializeBookReservedEvent() throws Exception {
		JsonNode json = objectMapper.readTree(loadResource(String.format(BOOK_SAMPLE_PATH, "book.reserved")));

		IntegrationEventEnvelope<BookLoanIntegrationPayload> event = objectMapper.treeToValue(json, new TypeReference<>() {});

		Assertions.assertEquals("BOOK_RESERVED", event.eventType().toString());
	}
	
	@Test
	void shouldDeserializeBookReserveRejectedEvent() throws Exception {
		JsonNode json = objectMapper.readTree(loadResource(String.format(BOOK_SAMPLE_PATH, "book.reservation.rejected")));

		IntegrationEventEnvelope<BookReservationRejectedIntegrationPayload> event = objectMapper.treeToValue(json, new TypeReference<>() {});

		Assertions.assertEquals("BOOK_RESERVATION_REJECTED", event.eventType().toString());
	}
	
	@Test
	void shouldDeserializeBookBorrowRejectedEvent() throws Exception {
		JsonNode json = objectMapper.readTree(loadResource(String.format(BOOK_SAMPLE_PATH, "book.borrow.rejected")));
		
		IntegrationEventEnvelope<BookBorrowRejectedIntegrationPayload> event = objectMapper.treeToValue(json, new TypeReference<>() {});

		Assertions.assertEquals("BOOK_BORROW_REJECTED", event.eventType().toString());
		
	}
	
	private void validateSample(String samplePath, String eventSchema) throws Exception {
		JsonNode sample = objectMapper.readTree(loadResource(samplePath));
		JsonSchema schema = loadSchema(eventSchema);

		Set<ValidationMessage> errors = schema.validate(sample);
		Assertions.assertTrue(errors.isEmpty(), errors.toString());
	}

	static Stream<Arguments> sampleCases() {
		return Stream.of(Arguments.of("book.borrowed", getBookContractCase("book.borrowed")),
				Arguments.of("book.reserved", getBookContractCase("book.reserved")),
				Arguments.of("book.reservation.rejected", getBookContractCase("book.reservation.rejected")),
				Arguments.of("book.borrow.rejected", getBookContractCase("book.borrow.rejected"))

		);
	}

	private static BookContractCase getBookContractCase(String eventName) {
		return new BookContractCase(eventName, String.format(BOOK_SCHEMA_PATH, eventName),
				String.format(BOOK_SAMPLE_PATH, eventName));
	}

	private JsonSchema loadSchema(String path) {
		JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
		return factory.getSchema(loadResource(path));
	}

	private InputStream loadResource(String path) {

		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);

		if (is == null) {
			throw new IllegalStateException(String.format("File not found in classpath: %s", path));
		}

		return is;
	}
}
