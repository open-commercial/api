ALTER TABLE ubicacion
Change piso piso varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL;

ALTER TABLE localidad
Change codigoPostal codigoPostal varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL;

SET SQL_SAFE_UPDATES = 0;
update localidad
set localidad.codigoPostal = null;
SET SQL_SAFE_UPDATES = 1;

ALTER TABLE ubicacion
Change numero numero int(11) DEFAULT NULL;