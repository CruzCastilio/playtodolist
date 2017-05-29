# Update table

# --- !Ups

ALTER TABLE task
  ADD COLUMN expirationDate VARCHAR(255) NOT NULL,
  ADD COLUMN task VARCHAR(255) NOT NULL
  AFTER label;

# --- !Downs

ALTER TABLE task
  DROP COLUMN expirationDate,
  DROP COLUMN label;
