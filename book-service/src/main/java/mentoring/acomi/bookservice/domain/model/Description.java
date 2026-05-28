package mentoring.acomi.bookservice.domain.model;

public class Description extends ValueObject<String>{

	public Description(String value)  {
		super(value);
	}

	@Override
	public String validate(String description) {
		return description == null ? "" : description.trim();
	}

}
