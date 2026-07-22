package mentoring.acomi.userservice.application.migration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("migration")
public class CardNumberMigrationRunner implements CommandLineRunner {

    private static final Logger logger = LogManager.getLogger(CardNumberMigrationRunner.class);

    private final CardNumberMigrationService service;

    public CardNumberMigrationRunner(CardNumberMigrationService service) {
        this.service = service;
    }

    @Override
    public void run(String... args) {

        logger.info("Starting card number migration...");

        service.migrate();

        logger.info("Card number migration completed.");
    }
}