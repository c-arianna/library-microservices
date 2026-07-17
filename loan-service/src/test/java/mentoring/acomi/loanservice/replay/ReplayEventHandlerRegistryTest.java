package mentoring.acomi.loanservice.replay;

import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import mentoring.acomi.loanservice.infrastructure.messaging.replay.ReplayEventHandlerRegistry;
import mentoring.acomi.loanservice.infrastructure.messaging.replay.ReplayLoanHandlerFactory;
import mentoring.acomi.sharedcorelibrary.integration.messaging.EventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.HandlerMetadata;
import mentoring.acomi.sharedcorelibrary.integration.messaging.HandlerMode;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.sharedcorelibrary.integration.messaging.NoOpEventHandler;

@ExtendWith(MockitoExtension.class)
public class ReplayEventHandlerRegistryTest {

	@Mock
	private ReplayLoanHandlerFactory factory;
	
	private ReplayEventHandlerRegistry registry;
	
	@BeforeEach
	void setUp() {
		when(factory.createHandlers()).thenReturn(List.of(new ReplayableLoanHandler()));
		registry = new ReplayEventHandlerRegistry(factory);
	}
    @Test
    void shouldFindReplayableHandler() {
        Optional<EventHandler> result = registry.find(event(IntegrationEventTypes.LOAN_REQUESTED, 1));
        Assertions.assertTrue(result.isPresent());
    }

    @Test
    void shouldNotFindUnsupportedVersion() {
        Optional<EventHandler> result = registry.find(event(IntegrationEventTypes.LOAN_REQUESTED, 999));
        Assertions.assertTrue(result.isEmpty());
    }

    @HandlerMetadata(eventType = IntegrationEventTypes.LOAN_REQUESTED, supportedVersions = {1}, mode = HandlerMode.REPLAYABLE)
    static class ReplayableLoanHandler extends NoOpEventHandler {}

    private IntegrationEventEnvelope<?> event(IntegrationEventTypes type, int version) {

        return new IntegrationEventEnvelope<>(UUID.randomUUID().toString(), type, "test", UUID.randomUUID().toString(), "LOAN",
                0, Instant.now(), version, null);
    }
}
