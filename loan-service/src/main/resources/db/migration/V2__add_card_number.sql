ALTER TABLE user_view ADD COLUMN name VARCHAR(100) after email;

ALTER TABLE user_view ADD COLUMN lastname VARCHAR(100) after name;

ALTER TABLE user_view ADD COLUMN card_number VARCHAR(20) after lastname;