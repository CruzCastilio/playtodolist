# Update table

# --- !Ups

ALTER TABLE task
  MODIFY COLUMN creationDate DATE NOT NULL,
  MODIFY COLUMN expirationDate DATE NOT NULL;

# --- !Downs

ALTER TABLE task
  MODIFY COLUMN creationDate VARCHAR(255) NOT NULL,
  MODIFY COLUMN expirationDate VARCHAR(255) NOT NULL;
