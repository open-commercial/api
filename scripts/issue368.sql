ALTER TABLE facturaventa
ADD idRemito bigint(20);

ALTER TABLE rengloncuentacorriente
ADD idRemito bigint(20);

CREATE TABLE `remito` (
  `idRemito` bigint(20) NOT NULL AUTO_INCREMENT,
  `cantidadDeBultos` decimal(19,2) DEFAULT NULL,
  `calleUbicacionCliente` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `categoriaIVACliente` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `codigoPostalLocalidadCliente` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `costoEnvioLocalidadCliente` decimal(19,2) DEFAULT NULL,
  `departamentoUbicacionCliente` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `descripcionUbicacionCliente` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `emailCliente` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `idFiscalCliente` bigint(20) DEFAULT NULL,
  `latitudUbicacionCliente` double DEFAULT NULL,
  `longitudUbicacionCliente` double DEFAULT NULL,
  `nombreFantasiaCliente` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `nombreFiscalCliente` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `nombreLocalidadCliente` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `nombreProvinciaCliente` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `nroCliente` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `numeroUbicacionCliente` int(11) DEFAULT NULL,
  `pisoUbicacionCliente` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `telefonoCliente` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `costoEnvioRemito` decimal(25,15) DEFAULT NULL,
  `calle` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `codigoPostal` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `costoDeEnvio` decimal(19,2) DEFAULT NULL,
  `departamento` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `descripcion` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `idLocalidad` bigint(20) DEFAULT NULL,
  `idProvincia` bigint(20) DEFAULT NULL,
  `idUbicacion` bigint(20) NOT NULL,
  `latitud` double DEFAULT NULL,
  `longitud` double DEFAULT NULL,
  `nombreLocalidad` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `nombreProvincia` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `numero` int(11) DEFAULT NULL,
  `piso` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `fecha` datetime DEFAULT NULL,
  `nroRemito` bigint(20) NOT NULL,
  `observaciones` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `pesoTotalEnKg` decimal(19,2) DEFAULT NULL,
  `serie` bigint(20) NOT NULL,
  `tipoComprobante` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `total` decimal(25,15) DEFAULT NULL,
  `totalFactura` decimal(25,15) DEFAULT NULL,
  `volumenTotalEnM3` decimal(19,2) DEFAULT NULL,
  `idCliente` bigint(20) NOT NULL,
  `idSucursal` bigint(20) NOT NULL,
  `id_Transportista` bigint(20) DEFAULT NULL,
  `id_Usuario` bigint(20) NOT NULL,
  PRIMARY KEY (`idRemito`),
  KEY `FKlqdfoon2fujsrsittfkyuvifl` (`idCliente`),
  KEY `FKt7istbgtw4tbbd0ba50glanqm` (`idSucursal`),
  KEY `FKjh88oboireovs0lx4cw8brbl` (`id_Transportista`),
  KEY `FKtfpu9u46j96s5j78emg4id51m` (`id_Usuario`),
  CONSTRAINT `FKjh88oboireovs0lx4cw8brbl` FOREIGN KEY (`id_Transportista`) REFERENCES `transportista` (`id_Transportista`),
  CONSTRAINT `FKlqdfoon2fujsrsittfkyuvifl` FOREIGN KEY (`idCliente`) REFERENCES `cliente` (`id_Cliente`),
  CONSTRAINT `FKt7istbgtw4tbbd0ba50glanqm` FOREIGN KEY (`idSucursal`) REFERENCES `sucursal` (`idSucursal`),
  CONSTRAINT `FKtfpu9u46j96s5j78emg4id51m` FOREIGN KEY (`id_Usuario`) REFERENCES `usuario` (`id_Usuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `renglonremito` (
  `idRenglonRemito` bigint(20) NOT NULL AUTO_INCREMENT,
  `cantidad` decimal(25,15) NOT NULL,
  `tipoBulto` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `idRemito` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idRenglonRemito`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

ALTER TABLE rengloncuentacorriente 
ADD CONSTRAINT `FKbkos7yyah1culti4mttsjh7eb` FOREIGN KEY (idRemito) REFERENCES remito(idRemito);