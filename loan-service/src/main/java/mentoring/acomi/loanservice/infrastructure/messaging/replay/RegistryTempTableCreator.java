package mentoring.acomi.loanservice.infrastructure.messaging.replay;

import java.util.Map;
import java.util.function.Consumer;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import mentoring.acomi.sharedcorelibrary.replay.TempTableCreator;

@Component
@Profile("H2")
public class RegistryTempTableCreator implements TempTableCreator {

	private final Map<String, Consumer<String>> creators;

	public RegistryTempTableCreator(UserH2TempTableCreator userCreator, LoanH2TempTableCreator loanCreator) {

		creators = Map.of("user_view", userCreator::createTempTableInternal, 
				          "loan_view", loanCreator::createTempTableInternal);

	}

	@Override
	public void createTempTable(String tmp, String main) {

		Consumer<String> creator = creators.get(main);

		if (creator == null) {
			throw new IllegalStateException("No creator for table %s".formatted(main));
		}

		creator.accept(tmp);
	}

}
