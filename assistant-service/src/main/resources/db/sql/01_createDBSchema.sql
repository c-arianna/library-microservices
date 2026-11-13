CREATE DATABASE IF NOT EXISTS assistant_db;

CREATE USER IF NOT EXISTS 'libraryApp' IDENTIFIED WITH mysql_native_password BY 'libraryAppTest!';
GRANT ALL PRIVILEGES ON assistant_db . * TO 'libraryApp'@'%';

FLUSH PRIVILEGES;