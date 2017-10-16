START TRANSACTION;

USE `sic`;
DROP TABLE IF EXISTS `pago`;

CREATE TABLE `pago` (
  `id_Pago` bigint(20) NOT NULL AUTO_INCREMENT,
  `eliminado` bit(1) NOT NULL,
  `fecha` datetime NOT NULL,
  `monto` double NOT NULL,
  `nota` varchar(255) NOT NULL,
  `id_Empresa` bigint(20) DEFAULT NULL,
  `id_Factura` bigint(20) DEFAULT NULL,
  `id_FormaDePago` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id_Pago`),
  KEY `FK346299A77FCB65` (`id_Factura`),
  KEY `FK3462996813BD87` (`id_Empresa`),
  KEY `FK346299C25D6C23` (`id_FormaDePago`),
  CONSTRAINT `FK3462996813BD87` FOREIGN KEY (`id_Empresa`) REFERENCES `empresa` (`id_Empresa`),
  CONSTRAINT `FK346299A77FCB65` FOREIGN KEY (`id_Factura`) REFERENCES `factura` (`id_Factura`),
  CONSTRAINT `FK346299C25D6C23` FOREIGN KEY (`id_FormaDePago`) REFERENCES `formadepago` (`id_FormaDePago`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

INSERT INTO pago
(eliminado, fecha, monto, id_Factura, id_Empresa, id_FormaDePago, nota)
SELECT eliminada, fecha, total, factura.id_Factura, id_Empresa, id_FormaDePago, ""
FROM facturaventa
INNER JOIN factura
ON factura.id_Factura=facturaventa.id_Factura;

INSERT INTO pago
(eliminado, fecha, monto, id_Factura, id_Empresa, id_FormaDePago, nota)
SELECT eliminada, pagofacturacompra.fecha, monto, pagofacturacompra.id_Factura, id_Empresa, id_FormaDePago, nota
FROM factura
INNER JOIN pagofacturacompra
ON factura.id_Factura=pagofacturacompra.id_Factura;

DROP TABLE pagofacturacompra;

ALTER TABLE `sic`.`factura` 
DROP FOREIGN KEY `FKBEEB4778C25D6C23`;

ALTER TABLE `sic`.`factura` 
DROP INDEX `FKBEEB4778C25D6C23` ;

ALTER TABLE `sic`.`factura`
DROP COLUMN id_FormaDePago;

COMMIT;
