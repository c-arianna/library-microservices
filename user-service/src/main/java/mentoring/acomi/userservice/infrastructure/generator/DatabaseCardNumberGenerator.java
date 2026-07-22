package mentoring.acomi.userservice.infrastructure.generator;

import org.springframework.stereotype.Component;

import mentoring.acomi.userservice.application.generator.CardNumberGenerator;
import mentoring.acomi.userservice.domain.model.CardNumber;
import mentoring.acomi.userservice.infrastructure.persistence.repositories.CardNumberSequenceRepository;

@Component
public class DatabaseCardNumberGenerator implements CardNumberGenerator {

    private final CardNumberSequenceRepository repository;
    
    public DatabaseCardNumberGenerator(CardNumberSequenceRepository repository) {
		this.repository = repository;
	}

	@Override
    public CardNumber generate() {
        long sequence = repository.nextValue();
        return new CardNumber("LIB-%06d".formatted(sequence));
    }
}