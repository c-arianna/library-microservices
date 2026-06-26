use book_db;

CREATE TABLE IF NOT EXISTS book_events (
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

CREATE TABLE IF NOT EXISTS book_view (
  isbn varchar(17) not null,
  author varchar(100) not null,
  title varchar(100) not null,
  description varchar(500),
  total_copies integer not null default 0,
  borrowed_copies integer not null default 0,
  reserved_copies integer not null default 0,
  created_at timestamp not null default current_timestamp,
  updated_at timestamp not null default current_timestamp,
  primary key (isbn)
);