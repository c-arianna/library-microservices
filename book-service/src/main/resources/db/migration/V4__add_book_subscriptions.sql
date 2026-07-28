create table book_subscriptions(
	id bigint not null auto_increment,
	isbn varchar(17) not null,
	user_identity_id varchar(36) not null,
	phone_number varchar(15) not null,
	subscription_type varchar(50) not null,
	status ENUM('ACTIVE', 'NOTIFIED') not null default 'ACTIVE',
	notified_at timestamp,
	created_at timestamp not null default current_timestamp,
	primary key (id),
	index idx_book_isbn(isbn),
	index idx_book_user(user_identity_id)
);