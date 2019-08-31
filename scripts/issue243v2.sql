ALTER TABLE facturaventa
ADD `categoriaIVACliente` varchar(255) DEFAULT NULL,
ADD `emailCliente` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
ADD `idFiscalCliente` bigint(20) DEFAULT NULL,
ADD `nombreFantasiaCliente` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
ADD `nombreFiscalCliente` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
ADD `nroCliente` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
ADD `telefonoCliente` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
ADD `calleUbicacionCliente` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
ADD `codigoPostalLocalidadCliente` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
ADD `departamentoUbicacionCliente` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
ADD `descripcionUbicacionCliente` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
ADD `latitudUbicacionCliente` double DEFAULT NULL,
ADD `longitudUbicacionCliente` double DEFAULT NULL,
ADD `nombreLocalidadCliente` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
ADD `costoEnvioLocalidadCliente` decimal(25,15) COLLATE utf8_unicode_ci DEFAULT NULL,
ADD `nombreProvinciaCliente` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
ADD `numeroUbicacionCliente` int(11) DEFAULT NULL,
ADD `pisoUbicacionCliente` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL;

SET SQL_SAFE_UPDATES = 0;

UPDATE
facturaventa fv inner join cliente c  
on c.id_Cliente = fv.id_Cliente
SET 
fv.categoriaIVACliente = c.categoriaIVA,
fv.emailCliente = c.email,
fv.idFiscalCliente = c.idFiscal,
fv.nroCliente = c.nroCliente,
fv.telefonoCliente = c.telefono,
fv.nombreFantasiaCliente = c.nombreFantasia,
fv.nombreFiscalCliente = c.nombreFiscal;

UPDATE
facturaventa fv inner join cliente c on c.id_Cliente = fv.id_Cliente
 inner join ubicacion
 on c.idUbicacionFacturacion = ubicacion.idUbicacion
 inner join localidad on localidad.idLocalidad = ubicacion.idLocalidad
 inner join provincia on provincia.idProvincia = localidad.idProvincia
SET 
fv.calleUbicacionCliente = ubicacion.calle,
fv.numeroUbicacionCliente = ubicacion.numero,
fv.codigoPostalLocalidadCliente = localidad.codigoPostal,
fv.departamentoUbicacionCliente = ubicacion.departamento,
fv.descripcionUbicacionCliente = ubicacion.descripcion,
fv.latitudUbicacionCliente = ubicacion.latitud,
fv.longitudUbicacionCliente = ubicacion.longitud,
fv.nombreLocalidadCliente = localidad.nombre,
fv.nombreProvinciaCliente = provincia.nombre,
fv.costoEnvioLocalidadCliente = localidad.costoEnvio;

SET SQL_SAFE_UPDATES = 1;

ALTER TABLE factura CHANGE `CAE` `cae` bigint(20);
ALTER TABLE nota CHANGE `CAE` `cae` bigint(20);
ALTER TABLE rengloncuentacorriente CHANGE `CAE` `cae` bigint(20); 
