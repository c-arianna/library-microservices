package mentoring.acomi.loanservice.infrastructure.messaging.handlers;

import org.springframework.stereotype.Component;

import mentoring.acomi.sharedcorelibrary.integration.messaging.HandlerMetadata;
import mentoring.acomi.sharedcorelibrary.integration.messaging.HandlerMode;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.sharedcorelibrary.integration.messaging.NoOpEventHandler;

@HandlerMetadata(eventType = IntegrationEventTypes.LOAN_CONFIRM_REQUESTED, supportedVersions = {1}, mode = HandlerMode.LIVE_ONLY)
@Component
public class LoanConfirmRequestedV1Handler extends NoOpEventHandler {

}