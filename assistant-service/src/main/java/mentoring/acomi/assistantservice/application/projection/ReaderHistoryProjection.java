package mentoring.acomi.assistantservice.application.projection;

import org.springframework.stereotype.Component;

import mentoring.acomi.assistantservice.application.repositories.ReaderHistoryViewRepository;
import mentoring.acomi.assistantservice.application.view.ReaderHistoryView;

@Component
public class ReaderHistoryProjection extends AbstractProjectionOperations<ReaderHistoryView>{
	
	public ReaderHistoryProjection(ReaderHistoryViewRepository repository) {
		 super(repository);
	}

}
