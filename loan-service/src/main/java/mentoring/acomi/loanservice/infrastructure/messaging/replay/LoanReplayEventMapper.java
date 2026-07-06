package mentoring.acomi.loanservice.infrastructure.messaging.replay;

import org.springframework.stereotype.Component;

import mentoring.acomi.loanservice.domain.events.LoanEventType;
import mentoring.acomi.loanservice.domain.events.payload.LoanFailedPayload;
import mentoring.acomi.loanservice.domain.events.payload.LoanPayload;
import mentoring.acomi.loanservice.domain.events.payload.LoanRequestPayload;
import mentoring.acomi.loanservice.domain.model.DateRange;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.producer.LoanFailedIntegrationPayload;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.producer.LoanIntegrationPayload;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.producer.LoanRequestedIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
public class LoanReplayEventMapper {

	private final ObjectMapper mapper;

	public LoanReplayEventMapper(ObjectMapper mapper) {
		this.mapper = mapper;
	}

	public IntegrationEventTypes toIntegrationEventType(LoanEventType eventType) {

		return switch (eventType) {
		case LoanRequested -> {
			yield IntegrationEventTypes.LOAN_REQUESTED;
		}
		case LoanConfirmed -> {
			yield IntegrationEventTypes.LOAN_CONFIRMED;
		}
		case LoanCanceled -> {
			yield IntegrationEventTypes.LOAN_CANCELED;
		}
		case LoanReturned -> {
			yield IntegrationEventTypes.LOAN_RETURNED;
		}
		case LoanReserved -> {
			yield IntegrationEventTypes.LOAN_RESERVED;
		}
		case LoanFailed -> {
			yield IntegrationEventTypes.LOAN_FAILED;
		}
		case LoanConfirmRequested -> {
			yield IntegrationEventTypes.LOAN_CONFIRM_REQUESTED;
		}

		};
	}

	public Object toIntegrationPayload(LoanEventType eventType, JsonNode eventPayload) {
		return switch (eventType) {
		case LoanRequested -> {
			LoanRequestPayload payload = mapper.convertValue(eventPayload, LoanRequestPayload.class);
			DateRange period = payload.period();
			yield new LoanRequestedIntegrationPayload(payload.id(), payload.isbn(), payload.userId(), period.getStart(),
					period.getEnd());
		}
		case LoanCanceled, LoanConfirmRequested, LoanConfirmed, LoanReserved, LoanReturned -> {
			LoanPayload payload = mapper.convertValue(eventPayload, LoanPayload.class);
			yield new LoanIntegrationPayload(payload.id(), payload.isbn(), payload.userId());
		}

		case LoanFailed -> {
			LoanFailedPayload payload = mapper.convertValue(eventPayload, LoanFailedPayload.class);
			yield new LoanFailedIntegrationPayload(payload.id(), payload.reason().toString());
		}

		};

	}

}
