package mentoring.acomi.sharedcorelibrary.replay;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class AbstractReplayService<E> {

	protected Logger logger = LogManager.getLogger(getClass());

	public void rebuild() {

		logger.info("Replay started");

		try {

			logger.info("Creating tmp table");
			createTempTable();

			List<E> events = loadEvents();
			logger.info("Loaded {} events", events.size());

			for (E event : events) {
				apply(event);
			}

			logger.info("Swapping tables");
			swapTables();

			logger.info("Replay completed");

		} catch (Exception e) {
			logger.error("Replay failed", e);
			dropTempTable();
			throw new RuntimeException("Replay failed", e);
		}
	}

	protected abstract void createTempTable();

	protected abstract List<E> loadEvents();

	protected abstract void apply(E event);

	protected abstract void swapTables();

	protected abstract void dropTempTable();
}
