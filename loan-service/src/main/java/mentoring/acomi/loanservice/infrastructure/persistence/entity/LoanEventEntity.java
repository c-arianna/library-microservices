package mentoring.acomi.loanservice.infrastructure.persistence.entity;

import java.time.Instant;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mentoring.acomi.sharedlibrary.eventstore.BaseEventEntity;
import tools.jackson.databind.JsonNode;

@Entity
@Table(name = "loan_events")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LoanEventEntity extends BaseEventEntity{

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(columnDefinition = "json")
	private JsonNode payload;

	public LoanEventEntity(String aggregateId, String eventId, String eventType, int eventVersion, JsonNode payload, Instant  occurredAt) {
		this.eventId = eventId;
		this.aggregateId = aggregateId;
		this.eventType = eventType;
		this.eventVersion = eventVersion;
		this.payload = payload;
		this.occurredAt = occurredAt;
	}

}
