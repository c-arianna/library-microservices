CREATE DATABASE IF NOT EXISTS user_db;

CREATE USER IF NOT EXISTS 'libraryApp' IDENTIFIED WITH mysql_native_password BY 'libraryAppTest!';
GRANT ALL PRIVILEGES ON user_db . * TO 'libraryApp'@'%';

FLUSH PRIVILEGES;