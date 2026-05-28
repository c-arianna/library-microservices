use loan_db;

CREATE TABLE IF NOT EXISTS loan_events (
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