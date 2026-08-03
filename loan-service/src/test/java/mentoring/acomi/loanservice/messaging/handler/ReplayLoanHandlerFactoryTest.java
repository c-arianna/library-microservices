package mentoring.acomi.loanservice.messaging.handler;

import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reflections.Reflections;

import mentoring.acomi.loanservice.application.projection.ProjectionDispatcher;
import mentoring.acomi.loanservice.infrastructure.messaging.replay.ReplayLoanHandlerFactory;
import mentoring.acomi.sharedcodelibrary.event.handlers.EventPayloadMapper;
import mentoring.acomi.sharedcorelibrary.integration.messaging.EventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.HandlerMetadata;
import mentoring.acomi.sharedcorelibrary.integration.messaging.HandlerMode;

@ExtendWith(MockitoExtension.class)
public class ReplayLoanHandlerFactoryTest {

    @Mock
    private ProjectionDispatcher dispatcher;
        
    @Mock
    private EventPayloadMapper mapper;

    private ReplayLoanHandlerFactory factory;

    @BeforeEach
    void setUp() {
        factory = new ReplayLoanHandlerFactory(dispatcher, mapper);
    }

    @Test
    void shouldCreateAllReplayableHandlers() {

        Set<Class<?>> expectedHandlers = findReplayableHandlers("mentoring.acomi.loanservice.infrastructure.messaging.handlers");
        Set<Class<?>> actualHandlers = factory.createHandlers().stream().map(Object::getClass).collect(Collectors.toSet());

        Assertions.assertFalse(expectedHandlers.isEmpty(), "No replayable handlers found");
        Assertions.assertEquals(expectedHandlers, actualHandlers);
    }

    private Set<Class<?>> findReplayableHandlers(String basePackage) {

        Reflections reflections = new Reflections(basePackage);

        return reflections.getTypesAnnotatedWith(HandlerMetadata.class).stream().filter(EventHandler.class::isAssignableFrom)
                .filter(this::isReplayable)
                .collect(Collectors.toSet());
    }

    private boolean isReplayable(Class<?> handlerClass) {
        HandlerMetadata metadata = handlerClass.getAnnotation(HandlerMetadata.class);
        return metadata != null && metadata.mode() == HandlerMode.REPLAYABLE;
    }
}