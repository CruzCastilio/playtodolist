# Users schema

# --- !Ups

CREATE TABLE users
(
  id INT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL,
  password VARCHAR(50) NOT NULL,
  isAdmin BOOLEAN NOT NULL,
  salt VARCHAR(50) NOT NULL
);
CREATE UNIQUE INDEX users_username_uindex ON users (username);