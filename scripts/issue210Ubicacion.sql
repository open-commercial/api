DROP TABLE pais;

CREATE TABLE `ubicacion` (
  `idUbicacion` bigint(20) NOT NULL AUTO_INCREMENT,
  `calle` varchar(255) COLLATE utf8_unicode_ci,
  `departamento` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `descripcion` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `eliminada` bit(1) NOT NULL,
  `latitud` double DEFAULT NULL,
  `longitud` double DEFAULT NULL,
  `numero` int(11),
  `piso` int(11) DEFAULT NULL,
  `id_Localidad` bigint(20) DEFAULT NULL,
  `id_Cliente` bigint(20) NOT NULL,
  PRIMARY KEY (`idUbicacion`),
  KEY `FKnt928a1oc3mtwe2spg8ov6qdm` (`id_Localidad`),
  CONSTRAINT `FKnt928a1oc3mtwe2spg8ov6qdm` FOREIGN KEY (`id_Localidad`) REFERENCES `localidad` (`id_Localidad`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;


INSERT INTO `ubicacion`(calle, numero, id_Localidad, id_Cliente, eliminada) 
select "ubicacionFacturacion", 123,localidad.id_Localidad, cliente.id_Cliente, cliente.eliminado from cliente inner join localidad
on cliente.id_Localidad = localidad.id_Localidad;

INSERT INTO `ubicacion`(calle, numero, id_Localidad, id_Cliente, eliminada) 
select "ubicacionEnvio", 123,localidad.id_Localidad, cliente.id_Cliente, cliente.eliminado from cliente inner join localidad
on cliente.id_Localidad = localidad.id_Localidad;

ALTER TABLE `cliente` 
ADD COLUMN `idUbicacionFacturacion` BIGINT(20) AFTER `bonificacion`;

ALTER TABLE `cliente` 
ADD COLUMN `idUbicacionEnvio` BIGINT(20) AFTER `idUbicacionFacturacion`;

UPDATE cliente c
INNER JOIN ubicacion u ON c.id_Cliente = u.id_Cliente
SET c.idUbicacionFacturacion = u.idUbicacion
where u.calle = "ubicacionFacturacion"; 

SET SQL_SAFE_UPDATES = 0;
UPDATE ubicacion u inner join cliente c on u.id_Cliente = c.id_Cliente
SET u.descripcion = c.direccion;
SET SQL_SAFE_UPDATES = 1;

UPDATE cliente c
INNER JOIN ubicacion u ON c.id_Cliente = u.id_Cliente
SET c.idUbicacionEnvio = u.idUbicacion
where u.calle = "ubicacionEnvio"; 

SET foreign_key_checks = 0;

ALTER TABLE `cliente`
ADD CONSTRAINT `FK838frolnqaeg8h97ggqu1rd67` FOREIGN KEY (`idUbicacionFacturacion`) 
REFERENCES `ubicacion`(`idUbicacion`);

ALTER TABLE `cliente`
ADD CONSTRAINT `FKkfnh6um3l9l5i0ywxwqr1qq9e` FOREIGN KEY (`idUbicacionEnvio`) 
REFERENCES `ubicacion`(`idUbicacion`);

SET foreign_key_checks = 1;

ALTER TABLE `ubicacion` 
DROP COLUMN `id_Cliente`;


ALTER TABLE `cliente` 
DROP FOREIGN KEY `FKc6sfncrbiypm57rdsn5gdoffe`;
ALTER TABLE `cliente` 
DROP COLUMN `id_Localidad`,
DROP INDEX `FKc6sfncrbiypm57rdsn5gdoffe` ;

ALTER TABLE `cliente` 
DROP COLUMN `direccion`;

ALTER TABLE `localidad` 
ADD COLUMN  `costoEnvio` decimal(25,15) DEFAULT NULL,
ADD COLUMN  `envioGratuito` bit(1) NOT NULL;