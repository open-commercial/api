ALTER TABLE usuario
DROP COLUMN passwordRecoveryKey;

ALTER TABLE usuario
ADD COLUMN passwordRecoveryKey varchar(255);
