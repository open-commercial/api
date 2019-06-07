ALTER TABLE facturaventa
ADD `categoriaIVA` varchar(255) DEFAULT NULL,
ADD `email` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
ADD `idFiscal` bigint(20) DEFAULT NULL,
ADD `nombreFantasia` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
ADD `nombreFiscal` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
ADD `nroCliente` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
ADD `telefono` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
ADD `calle` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
ADD `codigoPostal` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
ADD `departamento` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
ADD `descripcion` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
ADD `latitud` double DEFAULT NULL,
ADD `longitud` double DEFAULT NULL,
ADD `nombreLocalidad` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
ADD `nombreProvincia` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
ADD `numero` int(11) DEFAULT NULL,
ADD `piso` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL;

SET SQL_SAFE_UPDATES = 0;
UPDATE
facturaventa fv inner join cliente c  
on c.id_Cliente = fv.id_Cliente
SET 
fv.categoriaIVA = c.categoriaIVA,
fv.email = c.email,
fv.idFiscal = c.idFiscal,
fv.nroCliente = c.nroCliente,
fv.telefono = c.telefono;

UPDATE
facturaventa fv inner join cliente c  
on c.id_Cliente = fv.id_Cliente
inner join usuario usuarioCliente on c.id_Usuario_Credencial = usuarioCliente.id_Usuario
SET 
fv.categoriaIVA = c.categoriaIVA,
fv.email = c.email,
fv.idFiscal = c.idFiscal,
fv.nroCliente = c.nroCliente,
fv.telefono = c.telefono,
fv.nombreFantasia = c.nombreFantasia,
fv.nombreFiscal = c.nombreFiscal;

UPDATE
facturaventa fv inner join cliente c  
on c.id_Cliente = fv.id_Cliente
inner join usuario usuarioViajante on c.id_Usuario_Viajante = usuarioViajante.id_Usuario
SET 
fv.categoriaIVA = c.categoriaIVA,
fv.email = c.email,
fv.idFiscal = c.idFiscal,
fv.nroCliente = c.nroCliente,
fv.telefono = c.telefono,
fv.nombreFantasia = c.nombreFantasia,
fv.nombreFiscal = c.nombreFiscal;

UPDATE
facturaventa fv inner join cliente c  
on c.id_Cliente = fv.id_Cliente
inner join usuario usuarioCliente on c.id_Usuario_Credencial = usuarioCliente.id_Usuario
inner join usuario usuarioViajante on c.id_Usuario_Viajante = usuarioViajante.id_Usuario
SET 
fv.categoriaIVA = c.categoriaIVA,
fv.email = c.email,
fv.idFiscal = c.idFiscal,
fv.nroCliente = c.nroCliente,
fv.telefono = c.telefono,
fv.nombreFantasia = c.nombreFantasia,
fv.nombreFiscal = c.nombreFiscal;

UPDATE
facturaventa fv inner join cliente c on c.id_Cliente = fv.id_Cliente
 inner join ubicacion
 on c.idUbicacionFacturacion = ubicacion.idUbicacion
 inner join localidad on localidad.idLocalidad = ubicacion.idLocalidad
 inner join provincia on provincia.idProvincia = localidad.idProvincia
SET 
fv.calle = ubicacion.calle,
fv.codigoPostal = localidad.codigoPostal,
fv.departamento = ubicacion.departamento,
fv.descripcion = ubicacion.descripcion,
fv.latitud = ubicacion.latitud,
fv.longitud = ubicacion.longitud,
fv.nombreLocalidad = localidad.nombre,
fv.nombreProvincia = provincia.nombre;

SET SQL_SAFE_UPDATES = 1;

ALTER TABLE factura CHANGE `CAE` `cae` bigint(20);
ALTER TABLE nota CHANGE `CAE` `cae` bigint(20);
ALTER TABLE rengloncuentacorriente CHANGE `CAE` `cae` bigint(20);