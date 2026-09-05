package mentoring.acomi.sharedcorelibrary.eventstore.replay;

import java.util.List;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.replay.ReplayEventHandlerRegistry;
import mentoring.acomi.sharedcorelibrary.replay.ReplayException;

public abstract class AbstractReplayService<E> {

    protected final Logger logger = LogManager.getLogger(getClass());

    private final ReplayEventHandlerRegistry registry;
    private final List<ReplayProjection> replayProjections;
    
    protected AbstractReplayService(ReplayEventHandlerRegistry registry, List<ReplayProjection> replayProjections) {
        this.registry = registry;
        this.replayProjections = replayProjections;
    }

    public final void rebuild() {

        logger.info("Replay started");

        long startTime = System.currentTimeMillis();

        createReadModels();

        try {

            try (Stream<E> events = streamEvents()) {
                events.map(this::toIntegrationEvent).forEach(this::handle);
            }

            swapReadModels();

            logger.info("Replay completed in {} ms", System.currentTimeMillis() - startTime);

        } catch (Exception e) {
            cleanup();
            throw new ReplayException("Replay failed", e);
        }
    }

    private void handle(IntegrationEventEnvelope<?> event) {

        registry.find(event).ifPresentOrElse(handler -> handler.handleEvent(event),
                        () -> logger.info("Replay not needed for {}", event.eventType())
                );
    }

    protected void createReadModels() {
        replayProjections.forEach(ReplayProjection::createTempTable);
    }

    protected void swapReadModels() {
        replayProjections.forEach(ReplayProjection::swapTables);
    }

    protected void cleanup() {
        replayProjections.forEach(ReplayProjection::dropTempTable);
    }

    protected abstract Stream<E> streamEvents();

    protected abstract IntegrationEventEnvelope<?> toIntegrationEvent(E event);
    
}