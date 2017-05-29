# Tasks schema

# --- !Ups

CREATE TABLE task (
  id           INT          NOT NULL AUTO_INCREMENT,
  label        VARCHAR(255) NOT NULL,
  assigner     VARCHAR(255) NOT NULL,
  executor     VARCHAR(255) NOT NULL,
  creationDate VARCHAR(255) NOT NULL,
  PRIMARY KEY (id)
);

# --- !Downs

DROP TABLE task;
