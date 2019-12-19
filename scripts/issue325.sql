CREATE TABLE `tokenAcceso` (
  `idUsuario` bigint(20) NOT NULL,
  `aplicacion` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `token` varchar(300) COLLATE utf8_unicode_ci DEFAULT NULL,
  KEY `FK4tnomiey8b0ld6byk9h84i5id` (`idUsuario`),
  CONSTRAINT `FK4tnomiey8b0ld6byk9h84i5id` FOREIGN KEY (`idUsuario`) REFERENCES `usuario` (`id_Usuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

ALTER TABLE usuario DROP COLUMN token;