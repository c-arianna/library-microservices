CREATE TABLE IF NOT EXISTS user_view(
	id varchar(36) not null,
	email varchar(100) not null,
	user_identity_provider_id varchar(100) not null,
	status ENUM('ACTIVE', 'SUSPENDED', 'DISABLED') not null default 'ACTIVE',
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp,
	primary key(id)
);