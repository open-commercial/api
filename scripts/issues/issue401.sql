CREATE TABLE `tokenaccesoexcluido` (
  `idTokenAccesoExcluido` bigint NOT NULL AUTO_INCREMENT,
  `token` varchar(300) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`idTokenAccesoExcluido`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

DROP TABLE `tokenAcceso`;

