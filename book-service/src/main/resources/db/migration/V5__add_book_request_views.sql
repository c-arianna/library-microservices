CREATE TABLE IF NOT EXISTS book_request_view (
    request_id varchar(36) not null,
    requester_user_id varchar(36) not null,
    title varchar(100) not null,
    author varchar(100) not null,
    isbn varchar(17),
    notes varchar(1000),
    status ENUM('PENDING', 'APPROVED', 'REJECTED') not null default 'PENDING',
    votes int not null,
    created_at timestamp not null,
    updated_at timestamp not null,
    primary key(request_id)
);

CREATE TABLE IF NOT EXISTS book_request_vote_view (
	id bigint not null auto_increment,
    request_id varchar(36) not null,
    user_id varchar(36) not null,
    created_at timestamp not null,
    primary key(id),
    unique key uq_request_user (request_id, user_id)
);