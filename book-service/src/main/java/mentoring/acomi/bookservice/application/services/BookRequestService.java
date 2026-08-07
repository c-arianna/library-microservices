package mentoring.acomi.bookservice.application.services;

import java.util.UUID;

import org.springframework.stereotype.Service;

import mentoring.acomi.bookservice.application.aggregates.BookRequestAggregate;
import mentoring.acomi.bookservice.application.aggregates.BookRequestAggregateFactory;
import mentoring.acomi.bookservice.domain.bookrequest.model.BookRequest;
import mentoring.acomi.bookservice.infrastructure.dto.AddBookRequestDto;

@Service
public class BookRequestService {

	private final BookRequestAggregateFactory aggregateFactory;
	
	public BookRequestService(BookRequestAggregateFactory aggregateFactory) {
		this.aggregateFactory = aggregateFactory;
	}


	public void addRequest(AddBookRequestDto request) {
		
		String requestId = UUID.randomUUID().toString();
		
		BookRequest bookRequest = BookRequest.create(requestId, request.author(), request.title(), request.isbn(), requestId, request.notes());
		BookRequestAggregate aggregate = aggregateFactory.create(requestId);
		aggregate.add(bookRequest);
	}
}
