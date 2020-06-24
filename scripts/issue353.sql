SET SQL_SAFE_UPDATES = 0;

UPDATE pedido
SET pedido.estado = "CERRADO"
WHERE pedido.estado = "ACTIVO";

UPDATE pedido
SET pedido.estado = "CANCELADO"
WHERE pedido.estado = "ABIERTO";

ALTER TABLE pedido
ADD fechaVencimiento datetime after fecha;

UPDATE pedido
SET pedido.fechaVencimiento = pedido.fecha;

SET SQL_SAFE_UPDATES = 1;

ALTER TABLE configuracionsucursal
ADD predeterminada bit(1) after puntoDeRetiro;

UPDATE configuracionsucursal
SET predeterminada = 1
WHERE configuracionsucursal.idSucursal = 1;

UPDATE configuracionsucursal
SET predeterminada = 0
WHERE configuracionsucursal.idSucursal <> 1;

CREATE TABLE `traspaso` (
  `idTraspaso` bigint(20) NOT NULL AUTO_INCREMENT,
  `fechaDeAlta` datetime DEFAULT NULL,
  `nroPedido` bigint(20) DEFAULT NULL,
  `nroTraspaso` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `idSucursalDestino` bigint(20) NOT NULL,
  `idSucursalOrigen` bigint(20) NOT NULL,
  `id_Usuario` bigint(20) NOT NULL,
  PRIMARY KEY (`idTraspaso`),
  KEY `FKhibkv915isglkcjkj6qtlhbch` (`idSucursalDestino`),
  KEY `FKe8e6hd4na638nier9a3dopk7v` (`idSucursalOrigen`),
  KEY `FKo4uhogytva0p0bmx8sr4kxd85` (`id_Usuario`),
  CONSTRAINT `FKe8e6hd4na638nier9a3dopk7v` FOREIGN KEY (`idSucursalOrigen`) REFERENCES `sucursal` (`idSucursal`),
  CONSTRAINT `FKhibkv915isglkcjkj6qtlhbch` FOREIGN KEY (`idSucursalDestino`) REFERENCES `sucursal` (`idSucursal`),
  CONSTRAINT `FKo4uhogytva0p0bmx8sr4kxd85` FOREIGN KEY (`id_Usuario`) REFERENCES `usuario` (`id_Usuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `renglontraspaso` (
  `idRenglonTraspaso` bigint(20) NOT NULL AUTO_INCREMENT,
  `cantidadProducto` decimal(19,2) DEFAULT NULL,
  `codigoProducto` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `descripcionProducto` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `idProducto` bigint(20) NOT NULL,
  `nombreMedidaProducto` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `idTraspaso` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idRenglonTraspaso`),
  KEY `FKfrhlkj0h3rncjvqh76kgmq2u8` (`idTraspaso`),
  CONSTRAINT `FKfrhlkj0h3rncjvqh76kgmq2u8` FOREIGN KEY (`idTraspaso`) REFERENCES `traspaso` (`idTraspaso`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

