CREATE TABLE IF NOT EXISTS book_view (
  isbn varchar(17) not null,
  author varchar(100) not null,
  title varchar(100) not null,
  description text,
  total_copies integer not null default 0,
  borrowed_copies integer not null default 0,
  reserved_copies integer not null default 0,
  created_at timestamp not null default current_timestamp,
  updated_at timestamp not null default current_timestamp,
  primary key (isbn)
);