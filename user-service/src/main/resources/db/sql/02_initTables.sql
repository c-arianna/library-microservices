use user_db;

CREATE TABLE IF NOT EXISTS user_events (
    id bigint not null auto_increment,
    aggregate_id varchar(100) not null,
    event_type varchar(50) not null,
    event_id char(36) NOT NULL,
    event_version int not null,
    payload json not null,
    occurred_at timestamp not null,
    primary key (id),
    unique key uq_aggregate_version (aggregate_id, event_version),
    key idx_aggregate (aggregate_id),
    key idx_event_type (event_type)
);

CREATE TABLE IF NOT EXISTS user_view(
	id varchar(36) not null,
	email varchar(100) not null,
	name varchar(50) not null,
	lastname varchar(50) not null,
	password_hash varchar(100) not null,
	role ENUM('READER', 'LIBRARIAN', 'ADMIN') not null default 'READER',
	status ENUM('ACTIVE', 'SUSPENDED', 'DISABLE') not null default 'ACTIVE',
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp,
    unique key uq_email (email),
	primary key(id)
);


CREATE TABLE IF NOT EXISTS refresh_token (
    id bigint auto_increment,
    user_id varchar(36) not null,
    token varchar(128) null unique,
    expires_at datetime not null,
    revoked boolean not null,
    primary key(id)
);
