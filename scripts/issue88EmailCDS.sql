ALTER TABLE configuraciondelsistema
ADD emailSenderHabilitado bit(1) default false after signTokenWSAA;

ALTER TABLE configuraciondelsistema
ADD emailUsername varchar(255) after emailSenderHabilitado;

ALTER TABLE configuraciondelsistema
ADD emailPassword varchar(255) after emailUsername;
