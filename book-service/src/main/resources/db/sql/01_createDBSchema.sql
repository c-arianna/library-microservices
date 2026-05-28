CREATE DATABASE IF NOT EXISTS book_db;

CREATE USER IF NOT EXISTS 'libraryApp' IDENTIFIED WITH mysql_native_password BY 'libraryAppTest!';
GRANT ALL PRIVILEGES ON book_db . * TO 'libraryApp'@'%';

FLUSH PRIVILEGES;