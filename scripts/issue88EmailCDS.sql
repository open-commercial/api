ALTER TABLE configuraciondelsistema
ADD emailSenderHabilitado bit(1) after signTokenWSAA;

ALTER TABLE configuraciondelsistema
ADD emailUsername varchar(255) after EmailSenderHabilitado;

ALTER TABLE configuraciondelsistema
ADD emailPassword varchar(255) after EmailUsername;
