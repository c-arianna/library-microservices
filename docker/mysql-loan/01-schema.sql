use loan_db;

CREATE TABLE IF NOT EXISTS loan_events (
    id bigint not null auto_increment,
    aggregate_id varchar(100) not null,
	aggregate_type varchar(50) not null,
    event_type varchar(50) not null,
    event_id char(36) NOT NULL,
    event_version int not null,
	schema_version int,
	event_category ENUM('PRODUCER', 'CONSUMER') not null default 'PRODUCER',
    payload json not null,
	processed boolean not null default false,
	failed boolean not null default false,
    retry_count int not null default 0,
    occurred_at timestamp not null,
    primary key (id),
    unique key uq_aggregate_version (aggregate_id, event_version),
	unique key uq_aggregateType_version (event_id, aggregate_type),
    key idx_aggregate (aggregate_id),
    key idx_event_type (event_type)
);

CREATE TABLE IF NOT EXISTS loan_view (
  id varchar(36) not null,
  isbn varchar(17) not null,
  user_id varchar(36) not null,
  start_date date not null,
  end_date date not null,
  status ENUM('PENDING', 'RESERVED', 'CONFIRMED', 'CANCELED', 'RETURNED', 'FAILED') not null default 'PENDING',
  created_at timestamp not null default current_timestamp,
  updated_at timestamp not null default current_timestamp,
  primary key(id)
);

CREATE TABLE IF NOT EXISTS user_view(
	id varchar(36) not null,
	email varchar(100) not null,
	status ENUM('ACTIVE', 'SUSPENDED', 'DISABLE') not null default 'ACTIVE',
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp,
    unique key uq_email (email),
	primary key(id)
);