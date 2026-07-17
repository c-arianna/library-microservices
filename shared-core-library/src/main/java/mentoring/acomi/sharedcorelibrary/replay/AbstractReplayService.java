package mentoring.acomi.sharedcorelibrary.replay;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class AbstractReplayService<E> {

	protected Logger logger = LogManager.getLogger(getClass());

	public final void rebuild() {

		logger.info("Replay started");

		long start = System.currentTimeMillis();
		
		createTempTable();
		
		try {

			List<E> events = loadEvents();
			logger.info("Loaded {} events", events.size());

			for (E event : events) {
				try {
					apply(event);
				}catch (Exception e) {
					logger.error("Failed processing replay event {}", event, e);
					throw e;
				}
			}

			logger.info("Swapping tables");
			swapTables();

			logger.info("Replay completed in {} ms", System.currentTimeMillis() - start);

		} catch (Exception e) {
			logger.error("Replay failed", e);
			try {
				dropTempTable();
			}catch(Exception ex) {
				logger.error("Failed to drop temp table",ex);
			}
			
			throw new ReplayException("Replay failed", e);
		}
	}

	protected abstract void createTempTable();

	protected abstract List<E> loadEvents();

	protected abstract void apply(E event);

	protected abstract void swapTables();

	protected abstract void dropTempTable();
}
