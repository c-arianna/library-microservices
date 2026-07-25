create table outbox_event(
	event_id char(36) NOT NULL,
	aggregate_type varchar(50) not null,
	status ENUM('PENDING', 'PUBLISHED', 'FAILED') not null default 'PENDING',
	retry_count int default 0,
	last_error text,
	next_retry_at TIMESTAMP,
	created_at TIMESTAMP NOT NULL,
	published_at TIMESTAMP NULL,
	PRIMARY KEY (event_id)
);