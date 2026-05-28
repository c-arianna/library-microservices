CREATE DATABASE IF NOT EXISTS loan_db;

CREATE USER IF NOT EXISTS 'libraryApp' IDENTIFIED WITH mysql_native_password BY 'libraryAppTest!';
GRANT ALL PRIVILEGES ON loan_db . * TO 'libraryApp'@'%';

FLUSH PRIVILEGES;