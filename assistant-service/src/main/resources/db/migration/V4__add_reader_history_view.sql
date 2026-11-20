CREATE TABLE IF NOT EXISTS reader_history_view(
	loan_id varchar(36) not null,
	user_id varchar(36) not null,
	isbn varchar(17) not null,
	borrowed_at date not null,
	returned_at date,
	loan_status  ENUM('PENDING', 'RESERVED', 'CONFIRMED', 'CANCELED', 'RETURNED', 'FAILED') not null default 'PENDING',
	created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp,
	primary key(loan_id)
);