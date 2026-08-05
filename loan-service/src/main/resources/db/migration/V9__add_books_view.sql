CREATE TABLE IF NOT EXISTS book_view (
  isbn varchar(17) not null,
  author varchar(100) not null,
  title varchar(100) not null,
  primary key(isbn)
);

CREATE TABLE IF NOT EXISTS popular_book_view (
  isbn varchar(17) not null,
  author varchar(100) not null,
  title varchar(100) not null,
  loan_count int not null,
  primary key(isbn)
);