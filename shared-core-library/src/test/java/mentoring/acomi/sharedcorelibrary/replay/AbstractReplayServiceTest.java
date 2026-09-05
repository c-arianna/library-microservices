package mentoring.acomi.sharedcorelibrary.replay;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import mentoring.acomi.sharedcorelibrary.eventstore.replay.AbstractReplayService;
import mentoring.acomi.sharedcorelibrary.integration.messaging.EventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;

@ExtendWith(MockitoExtension.class)
public class AbstractReplayServiceTest {

    @Mock
    private ReplayEventHandlerRegistry registry;

    @Mock
    private EventHandler eventHandler;

    @Test
    void shouldExecuteReplayStepsInOrder() {

        TestReplayService service = new TestReplayService(registry);

        when(registry.find(any())).thenReturn(Optional.of(eventHandler));
        
        service.rebuild();

        Assertions.assertEquals(List.of("create", "load", "map:1", "map:2", "swap"), service.operations());

        verify(eventHandler, times(2)).handleEvent(any());
    }

    @Test
    void shouldCleanupWhenReplayFails() {

        FailingReplayService service = new FailingReplayService(registry);

        Assertions.assertThrows(ReplayException.class, service::rebuild);
        Assertions.assertEquals(List.of("create", "load", "map", "drop"), service.operations());
    }

    static class TestReplayService extends AbstractReplayService<Integer> {

        private final List<String> operations = new ArrayList<>();

        TestReplayService(ReplayEventHandlerRegistry registry){
            super(registry, List.of());
        }

        List<String> operations() {
            return operations;
        }

        @Override
        protected void createReadModels() {
            operations.add("create");
        }

        @Override
        protected Stream<Integer> streamEvents() {
            operations.add("load");
            return Stream.of(1, 2);
        }

        @Override
        protected IntegrationEventEnvelope<?> toIntegrationEvent(Integer event) {
            operations.add("map:%d".formatted(event));
            return envelope();
        }

        @Override
        protected void swapReadModels() {
            operations.add("swap");
        }

        @Override
        protected void cleanup() {
            operations.add("drop");
        }
    }

    static class FailingReplayService extends AbstractReplayService<Integer> {

        private final List<String> operations = new ArrayList<>();

        FailingReplayService(ReplayEventHandlerRegistry registry) {
            super(registry, List.of());
        }

        List<String> operations() {
            return operations;
        }

        @Override
        protected void createReadModels() {
            operations.add("create");
        }

        @Override
        protected Stream<Integer> streamEvents() {
            operations.add("load");
            return Stream.of(1);
        }

        @Override
        protected IntegrationEventEnvelope<?> toIntegrationEvent(Integer event) {
            operations.add("map");
            throw new RuntimeException("failure");
        }

        @Override
        protected void swapReadModels() {
            operations.add("swap");
        }

        @Override
        protected void cleanup() {
            operations.add("drop");
        }
    }

    private static IntegrationEventEnvelope<?> envelope() {
        return new IntegrationEventEnvelope<>(UUID.randomUUID().toString(), IntegrationEventTypes.BOOK_REGISTERED, "test",
                UUID.randomUUID().toString(), "BOOK", 0, Instant.now(), 1, Map.of());
    }
}