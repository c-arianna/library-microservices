package mentoring.acomi.assistantservice.semantic;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import mentoring.acomi.assistantservice.application.dto.AnalyticsQueryResponse;
import mentoring.acomi.assistantservice.application.model.DashboardType;
import mentoring.acomi.assistantservice.application.services.analytics.AnalitycsService;

@SpringBootTest
@Tag("semantic")
@ActiveProfiles(profiles = {"semantic", "test"})
public class AnalyticsSemanticTest {
	
	@Autowired
	private AnalitycsService service;
	
	@Test
	public void shouldLoadDailyLoanStatistics() {
		
		String query = "Mostrami l'andamento dei prestiti degli ultimi 90 giorni";
		
		AnalyticsQueryResponse analyticsData = service.loadAnalyticsData(query);
		
		assertEquals(DashboardType.DAILY_LOANS, analyticsData.dashboard());
		
		assertNotNull(analyticsData.lastDays());
		assertEquals(90, analyticsData.lastDays());
				
		assertNull(analyticsData.limit());
		
		assertNotNull(analyticsData.explanation());
		assertFalse(analyticsData.explanation().isBlank());
		
	}
	
	@Test
	public void shouldLoadPopularBooks() {
		
        String query = "Quali sono i 10 libri più richiesti?";
		
		AnalyticsQueryResponse analyticsData = service.loadAnalyticsData(query);
		
		assertEquals(DashboardType.POPULAR_BOOKS, analyticsData.dashboard());
		
		assertNull(analyticsData.lastDays());
				
		assertNotNull(analyticsData.limit());
		assertEquals(10, analyticsData.limit());
		
		assertNotNull(analyticsData.explanation());
		assertFalse(analyticsData.explanation().isBlank());
		
	}
	
	@Test
	public void shouldLoadOverdueLoans() {
		
        String query = "Mostrami i prestiti in ritardo";
		
		AnalyticsQueryResponse analyticsData = service.loadAnalyticsData(query);
		
		assertEquals(DashboardType.OVERDUE_LOANS, analyticsData.dashboard());
		
		assertNull(analyticsData.lastDays());
				
		assertNull(analyticsData.limit());
				
		assertNotNull(analyticsData.explanation());
		assertFalse(analyticsData.explanation().isBlank());
		
	}
	
	@Test
	public void shouldLoadOverdueStatistics() {
		
        String query = "Mostrami lo storico dei prestiti in ritardo per utente";
		
		AnalyticsQueryResponse analyticsData = service.loadAnalyticsData(query);
		
		assertEquals(DashboardType.USER_LOAN_STATISTICS, analyticsData.dashboard());
		
		assertNull(analyticsData.lastDays());
				
		assertNull(analyticsData.limit());
				
		assertNotNull(analyticsData.explanation() == null);
		assertFalse(analyticsData.explanation().isBlank());
		
	}
	
	@Test
	public void shouldReturnUnsupportedResponse() {
		
        String query = "Mostrami l'andamento degli acquisti di libri nell'ultimo mese";
		
		AnalyticsQueryResponse analyticsData = service.loadAnalyticsData(query);
		
		assertEquals(DashboardType.UNSUPPORTED, analyticsData.dashboard());
		
		assertNull(analyticsData.lastDays());
				
		assertNull(analyticsData.limit());
				
		assertNotNull(analyticsData.explanation());
		assertFalse(analyticsData.explanation().isBlank());
		
	}

}
