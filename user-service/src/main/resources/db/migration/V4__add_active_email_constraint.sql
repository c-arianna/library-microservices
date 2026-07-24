ALTER TABLE user_view DROP INDEX uq_email;

ALTER TABLE user_view ADD COLUMN active_email VARCHAR(100) after status;

UPDATE user_view SET active_email = email WHERE status <> 'DISABLED';

CREATE UNIQUE INDEX uk_user_active_email ON user_view(active_email);