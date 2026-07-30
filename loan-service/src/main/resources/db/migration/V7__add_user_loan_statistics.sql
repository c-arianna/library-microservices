CREATE TABLE user_loan_statistics(
	user_id varchar(36) not null,
	overdue_loans_count int not null,
	total_days_overdue int not null,
	last_overdue_date date,
	primary key(user_id)
);