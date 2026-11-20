package mentoring.acomi.assistantservice.messaging.notifications.handlers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import mentoring.acomi.assistantservice.application.projection.ProjectionOperations;
import mentoring.acomi.assistantservice.application.view.ReaderHistoryView;
import mentoring.acomi.assistantservice.infrastructure.messaging.notifications.handlers.LoanUpdatedV1NotificationHandler;
import mentoring.acomi.assistantservice.infrastructure.messaging.notifications.payload.LoanUpdatedNotificationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventType;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationHandler;
import mentoring.acomi.sharedcorelibrary.model.LoanStatus;

@ExtendWith(MockitoExtension.class)
public class LoanUpdatedV1NotificationHandlerTest extends AbstractNotificationHandlerTest {

	private static final String LOAN_ID = "loanId1";
	
	private static final String TITLE = "Il barone rampante";

	private static final String AUTHOR = "Italo Calvino";

	private static final String ISBN = "9788804336327";
	
	private static final String USER_ID = "userId1";
	
	private static final String IP_ID = "ip1";
	
	private static final String CARD_NUMBER = "CARD-00001";
	
	@Mock
	private ProjectionOperations<ReaderHistoryView> projection;
	
	private LoanUpdatedV1NotificationHandler handler;

	@BeforeEach
	void setUp() {
		handler = new LoanUpdatedV1NotificationHandler(projection, mapper);
	}
	
	@Override
	protected NotificationHandler handler() {
		return handler;
	}

	@Override
	protected NotificationEventEnvelope<LoanUpdatedNotificationPayload> validEvent() {
		return getLoanUpdatedEvent(LOAN_ID, ISBN, AUTHOR, TITLE, USER_ID, IP_ID, CARD_NUMBER, LoanStatus.PENDING, LocalDate.now(), 
				LocalDate.now().plusDays(30), 1);
	}
	
	@TestFactory
	Collection<DynamicTest> shouldRejectInvalidPayloads() {
		return invalidPayloads().stream().map(scenario -> DynamicTest.dynamicTest(scenario.description(), 
				     () -> assertInvalidPayload(scenario.event(), scenario.field(), projection))).toList();
	
	}
	
	private List<InvalidNotificationPayloadScenario> invalidPayloads() {
	    return List.of(new InvalidNotificationPayloadScenario("blank loanId", "loanId",
	    				getLoanUpdatedEvent("", ISBN, AUTHOR, TITLE, USER_ID, IP_ID, CARD_NUMBER, LoanStatus.PENDING, LocalDate.now(), 
	    						LocalDate.now().plusDays(30), 1)),
	    			  new InvalidNotificationPayloadScenario("blank isbn", "isbn",
	    	    		getLoanUpdatedEvent(LOAN_ID, "", AUTHOR, TITLE, USER_ID, IP_ID, CARD_NUMBER, LoanStatus.PENDING, LocalDate.now(), 
	    	    				LocalDate.now().plusDays(30), 1)),
	    		       new InvalidNotificationPayloadScenario("blank author", "author",
	    		    	getLoanUpdatedEvent(LOAN_ID, ISBN, "", TITLE, USER_ID, IP_ID, CARD_NUMBER, LoanStatus.PENDING, LocalDate.now(), 
	   	    	    		    LocalDate.now().plusDays(30), 1)),
	    		       new InvalidNotificationPayloadScenario("blank title", "title",
	    		        getLoanUpdatedEvent(LOAN_ID, ISBN, AUTHOR, "", USER_ID, IP_ID, CARD_NUMBER, LoanStatus.PENDING, LocalDate.now(), 
	   	    	    			LocalDate.now().plusDays(30), 1)),
	    		       new InvalidNotificationPayloadScenario("blank userId", "userId",
	    		        getLoanUpdatedEvent(LOAN_ID, ISBN, AUTHOR, TITLE, "", IP_ID, CARD_NUMBER, LoanStatus.PENDING, LocalDate.now(), 
	   	    	    			LocalDate.now().plusDays(30), 1)),
	    		       new InvalidNotificationPayloadScenario("blank identityProviderId", "identityProviderId",
	   	    		    getLoanUpdatedEvent(LOAN_ID, ISBN, AUTHOR, TITLE, USER_ID, "", CARD_NUMBER, LoanStatus.PENDING, LocalDate.now(), 
	   	   	    	    		LocalDate.now().plusDays(30), 1)),
	    		       new InvalidNotificationPayloadScenario("null status", "status",
		   	    		getLoanUpdatedEvent(LOAN_ID, ISBN, AUTHOR, TITLE, USER_ID, IP_ID, CARD_NUMBER, null, LocalDate.now(), 
		   	   	    	    	LocalDate.now().plusDays(30), 1)),
	    		       new InvalidNotificationPayloadScenario("null startDate", "startDate",
		   	    		getLoanUpdatedEvent(LOAN_ID, ISBN, AUTHOR, TITLE, USER_ID, IP_ID, CARD_NUMBER, LoanStatus.PENDING, null, 
		   	   	    	        LocalDate.now().plusDays(30), 1)),
	    		       new InvalidNotificationPayloadScenario("null endDate", "endDate",
	   		   	        getLoanUpdatedEvent(LOAN_ID, ISBN, AUTHOR, TITLE, USER_ID, IP_ID, CARD_NUMBER, LoanStatus.PENDING, LocalDate.now(), 
	   		   	   	    	    null, 1)));
	}
	
	@Test
	void shouldHandleBookSubscriptionNotifiedEvent() {
		NotificationEventEnvelope<LoanUpdatedNotificationPayload> event = validEvent();
		handler.handleEvent(event);
		verify(projection, times(1)).upsert(eq(handler.getView(event.payload())), any(Instant.class));
	}
		
	
	private NotificationEventEnvelope<LoanUpdatedNotificationPayload> getLoanUpdatedEvent(String loanId, String isbn, String author,
			String title, String userId, String identityProviderId, String cardNumber, LoanStatus status, LocalDate startDate, 
			LocalDate endDate, int schemaVersion) {
		
		return new NotificationEventEnvelope<>(UUID.randomUUID().toString(), NotificationEventType.LOAN_UPDATED, "test-handler", 
				Instant.now(), schemaVersion, getPayload(loanId, isbn, author, title, userId, identityProviderId, cardNumber, status,
						startDate, endDate));
	}

	private LoanUpdatedNotificationPayload getPayload(String loanId, String isbn, String author,
			String title, String userId, String identityProviderId, String cardNumber, LoanStatus status, LocalDate startDate, 
			LocalDate endDate) {
		return new LoanUpdatedNotificationPayload(loanId, isbn, author, title, userId, identityProviderId, cardNumber, status, startDate,
				endDate, false, 0);
	}
}
