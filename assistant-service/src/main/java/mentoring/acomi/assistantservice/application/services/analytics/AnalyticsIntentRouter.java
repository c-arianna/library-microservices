package mentoring.acomi.assistantservice.application.services.analytics;

import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Component;

import mentoring.acomi.assistantservice.application.model.DashboardType;

@Component
public class AnalyticsIntentRouter {

	private static final Set<String> UNSUPPORTED_KEYWORDS = Set.of("iscrizioni", "acquisto", "acquisti", "purchase");

	public Optional<DashboardType> resolveDashboardType(String question) {

		String text = question.toLowerCase();

		if (containsUnsupportedKeywords(text)) {
			return Optional.of(DashboardType.UNSUPPORTED);
		}
		
		if (text.contains("ritardo per utente") || text.contains("storico ritardi")
				|| text.contains("statistiche utenti")) {
			return Optional.of(DashboardType.USER_LOAN_STATISTICS);
		}

		if (text.contains("prestiti in ritardo") || text.contains("prestiti scaduti")) {
			return Optional.of(DashboardType.OVERDUE_LOANS);
		}

		if (text.contains("libri più richiesti") || text.contains("libri più popolari") || text.contains("top libri")) {
			return Optional.of(DashboardType.POPULAR_BOOKS);
		}

		return Optional.empty();
	}

	private boolean containsUnsupportedKeywords(String query) {
		return UNSUPPORTED_KEYWORDS.stream().anyMatch(query::contains);
	}
}