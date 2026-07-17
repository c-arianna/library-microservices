package mentoring.acomi.sharedcorelibrary.replay;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AbstractReplayServiceTest {

    @Test
    void shouldExecuteReplayStepsInOrder() {

        TestReplayService service = new TestReplayService();

        service.rebuild();

        Assertions.assertEquals(List.of("create", "load", "apply:1", "apply:2", "swap"), service.operations());
    }

    @Test
    void shouldDropTempTableWhenReplayFails() {

        FailingReplayService service = new FailingReplayService();

        Assertions.assertThrows(ReplayException.class, service::rebuild);

        Assertions.assertEquals(List.of("create", "load", "apply", "drop"), service.operations());
    }

    static class TestReplayService extends AbstractReplayService<Integer> {

        private final List<String> operations = new ArrayList<>();

        List<String> operations() {
            return operations;
        }

        @Override
        protected void createTempTable() {
            operations.add("create");
        }

        @Override
        protected List<Integer> loadEvents() {
            operations.add("load");
            return List.of(1, 2);
        }

        @Override
        protected void apply(Integer event) {
            operations.add("apply:%d".formatted(event));
        }

        @Override
        protected void swapTables() {
            operations.add("swap");
        }

        @Override
        protected void dropTempTable() {
            operations.add("drop");
        }
    }

    static class FailingReplayService extends AbstractReplayService<Integer> {

        private final List<String> operations = new ArrayList<>();

        List<String> operations() {
            return operations;
        }

        @Override
        protected void createTempTable() {
            operations.add("create");
        }

        @Override
        protected List<Integer> loadEvents() {
            operations.add("load");
            return List.of(1);
        }

        @Override
        protected void apply(Integer event) {
            operations.add("apply");
            throw new RuntimeException("failure");
        }

        @Override
        protected void swapTables() {
            operations.add("swap");
        }

        @Override
        protected void dropTempTable() {
            operations.add("drop");
        }
    }
}