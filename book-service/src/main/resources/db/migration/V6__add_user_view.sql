CREATE TABLE IF NOT EXISTS user_view(
	id varchar(36) not null,
	email varchar(100) not null,
	name varchar(50) not null,
	lastname varchar(50) not null,
	card_number varchar(20),
	user_identity_provider_id varchar(100) not null,
	status ENUM('ACTIVE', 'DISABLED') not null default 'ACTIVE',
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp,
	primary key(id)
);