CREATE TABLE daily_loan_statistics (
    statistics_date date not null,
    loans_created int not null,
    loans_confirmed int not null,
    loans_canceled int not null,
    loans_returned int not null,
    primary key(statistics_date)
);