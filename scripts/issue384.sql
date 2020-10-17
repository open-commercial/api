CREATE TABLE `productofavorito` (
  `idProductoFavorito` bigint(20) NOT NULL AUTO_INCREMENT,
  `id_Cliente` bigint(20) NOT NULL,
  `idProducto` bigint(20) NOT NULL,
  PRIMARY KEY (`idProductoFavorito`),
  KEY `FKlj6v8a1tyy2ggfr6dsn72gwib` (`id_Cliente`),
  KEY `FKef99hvwh8hfwrui59x209a79a` (`idProducto`),
  CONSTRAINT `FKef99hvwh8hfwrui59x209a79a` FOREIGN KEY (`idProducto`) REFERENCES `producto` (`idProducto`),
  CONSTRAINT `FKlj6v8a1tyy2ggfr6dsn72gwib` FOREIGN KEY (`id_Cliente`) REFERENCES `cliente` (`id_Cliente`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
