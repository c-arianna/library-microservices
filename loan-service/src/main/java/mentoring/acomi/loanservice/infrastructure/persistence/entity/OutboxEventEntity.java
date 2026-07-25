package mentoring.acomi.loanservice.infrastructure.persistence.entity;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mentoring.acomi.sharedcorelibrary.outbox.OutboxStatus;

@Entity
@Table(name = "outbox_event")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboxEventEntity {
		
	@Id
    private String eventId;

    private String aggregateType;
    
    @Enumerated(EnumType.STRING)
	private OutboxStatus status;
    
    private int retryCount;
    
    private String lastError;
    
    private Instant nextRetryAt;
    
    private Instant createdAt;
        
    private Instant publishedAt;
    
    public OutboxEventEntity(String eventId, String aggregateType, OutboxStatus status, int retryCount, String lastError,
    		Instant createdAt, Instant publishedAt, Instant nextRetryAt) {
    	this.eventId = eventId;
    	this.aggregateType = aggregateType;
    	this.retryCount = retryCount;
    	this.status = status;
    	this.lastError = lastError;
    	this.createdAt = createdAt;
    	this.nextRetryAt = nextRetryAt;
    	this.publishedAt = publishedAt;
    }
        
}

