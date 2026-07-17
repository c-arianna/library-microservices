package mentoring.acomi.bookservice.infrastructure.messaging.handlers;

import org.springframework.stereotype.Component;

import mentoring.acomi.sharedcorelibrary.integration.messaging.HandlerMetadata;
import mentoring.acomi.sharedcorelibrary.integration.messaging.HandlerMode;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.sharedcorelibrary.integration.messaging.NoOpEventHandler;

@HandlerMetadata(eventType = IntegrationEventTypes.BOOK_BORROW_REJECTED, supportedVersions = {1}, mode = HandlerMode.LIVE_ONLY)
@Component
public class BookBorrowRejectedV1Handler extends NoOpEventHandler {


}