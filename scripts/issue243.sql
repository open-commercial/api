ALTER TABLE facturaventa
ADD `bonificacion` decimal(19,2) DEFAULT NULL,
ADD `categoriaIVA` varchar(255) DEFAULT NULL,
ADD `contacto` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
ADD `email` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
ADD `fechaAlta` datetime DEFAULT NULL,
ADD `idCredencial` bigint(20) DEFAULT NULL,
ADD `idEmpresa` bigint(20) DEFAULT NULL,
ADD `idFiscal` bigint(20) DEFAULT NULL,
ADD `idViajante` bigint(20) DEFAULT NULL,
ADD `idClienteEmbedded` bigint(20) NOT NULL,
ADD `nombreCredencial` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
ADD `nombreEmpresa` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
ADD `nombreFantasia` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
ADD `nombreFiscal` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
ADD `nombreViajante` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
ADD `nroCliente` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
ADD `predeterminado` bit(1) NOT NULL,
ADD `telefono` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
ADD `calleEnvio` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
ADD `codigoPostalEnvio` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
ADD `departamentoEnvio` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
ADD `descripcionEnvio` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
ADD `idLocalidadEnvio` bigint(20) DEFAULT NULL,
ADD `idProvinciaEnvio` bigint(20) DEFAULT NULL,
ADD `idUbicacionEnvio` bigint(20) DEFAULT NULL,
ADD `latitudEnvio` double DEFAULT NULL,
ADD `longitudEnvio` double DEFAULT NULL,
ADD `nombreLocalidadEnvio` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
ADD `nombreProvinciaEnvio` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
ADD `numeroEnvio` int(11) DEFAULT NULL,
ADD `pisoEnvio` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
ADD `calleFacturacion` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
ADD `codigoPostalFacturacion` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
ADD `departamentoFacturacion` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
ADD `descripcionFacturacion` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
ADD `idLocalidadFacturacion` bigint(20) DEFAULT NULL,
ADD `idProvinciaFacturacion` bigint(20) DEFAULT NULL,
ADD `idUbicacionFacturacion` bigint(20) DEFAULT NULL,
ADD `latitudFacturacion` double DEFAULT NULL,
ADD `longitudFacturacion` double DEFAULT NULL,
ADD `nombreLocalidadFacturacion` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
ADD `nombreProvinciaFacturacion` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
ADD `numeroFacturacion` int(11) DEFAULT NULL,
ADD `pisoFacturacion` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL;

SET SQL_SAFE_UPDATES = 0;
UPDATE
facturaventa fv inner join cliente c  
on c.id_Cliente = fv.id_Cliente
inner join empresa on c.id_Empresa = empresa.id_Empresa
SET 
fv.bonificacion = c.bonificacion,
fv.categoriaIVA = c.categoriaIVA,
fv.contacto = c.contacto,
fv.email = c.email,
fv.fechaAlta = c.fechaAlta,
fv.idCredencial = c.id_Usuario_Credencial,
fv.idEmpresa = c.id_Empresa,
fv.idFiscal = c.idFiscal,
fv.idViajante = c.id_Usuario_Viajante,
fv.nroCliente = c.nroCliente,
fv.predeterminado = c.predeterminado,
fv.telefono = c.telefono,
fv.nombreEmpresa = empresa.nombre;

UPDATE
facturaventa fv inner join cliente c  
on c.id_Cliente = fv.id_Cliente
inner join usuario usuarioCliente on c.id_Usuario_Credencial = usuarioCliente.id_Usuario
inner join empresa on c.id_Empresa = empresa.id_Empresa
SET 
fv.bonificacion = c.bonificacion,
fv.categoriaIVA = c.categoriaIVA,
fv.contacto = c.contacto,
fv.email = c.email,
fv.fechaAlta = c.fechaAlta,
fv.idCredencial = c.id_Usuario_Credencial,
fv.idEmpresa = c.id_Empresa,
fv.idFiscal = c.idFiscal,
fv.idViajante = c.id_Usuario_Viajante,
fv.nroCliente = c.nroCliente,
fv.predeterminado = c.predeterminado,
fv.telefono = c.telefono,
fv.nombreCredencial = usuarioCliente.nombre,
fv.nombreEmpresa = empresa.nombre,
fv.nombreFantasia = c.nombreFantasia,
fv.nombreFiscal = c.nombreFiscal;

UPDATE
facturaventa fv inner join cliente c  
on c.id_Cliente = fv.id_Cliente
inner join usuario usuarioViajante on c.id_Usuario_Viajante = usuarioViajante.id_Usuario
inner join empresa on c.id_Empresa = empresa.id_Empresa
SET 
fv.bonificacion = c.bonificacion,
fv.categoriaIVA = c.categoriaIVA,
fv.contacto = c.contacto,
fv.email = c.email,
fv.fechaAlta = c.fechaAlta,
fv.idCredencial = c.id_Usuario_Credencial,
fv.idEmpresa = c.id_Empresa,
fv.idFiscal = c.idFiscal,
fv.idViajante = c.id_Usuario_Viajante,
fv.nroCliente = c.nroCliente,
fv.predeterminado = c.predeterminado,
fv.telefono = c.telefono,
fv.nombreEmpresa = empresa.nombre,
fv.nombreFantasia = c.nombreFantasia,
fv.nombreFiscal = c.nombreFiscal,
fv.nombreViajante = usuarioViajante.nombre;

UPDATE
facturaventa fv inner join cliente c  
on c.id_Cliente = fv.id_Cliente
inner join usuario usuarioCliente on c.id_Usuario_Credencial = usuarioCliente.id_Usuario
inner join usuario usuarioViajante on c.id_Usuario_Viajante = usuarioViajante.id_Usuario
inner join empresa on c.id_Empresa = empresa.id_Empresa
SET 
fv.bonificacion = c.bonificacion,
fv.categoriaIVA = c.categoriaIVA,
fv.contacto = c.contacto,
fv.email = c.email,
fv.fechaAlta = c.fechaAlta,
fv.idCredencial = c.id_Usuario_Credencial,
fv.idEmpresa = c.id_Empresa,
fv.idFiscal = c.idFiscal,
fv.idViajante = c.id_Usuario_Viajante,
fv.nroCliente = c.nroCliente,
fv.predeterminado = c.predeterminado,
fv.telefono = c.telefono,
fv.nombreCredencial = usuarioCliente.nombre,
fv.nombreEmpresa = empresa.nombre,
fv.nombreFantasia = c.nombreFantasia,
fv.nombreFiscal = c.nombreFiscal,
fv.nombreViajante = usuarioViajante.nombre;

UPDATE
facturaventa fv inner join cliente c on c.id_Cliente = fv.id_Cliente
 inner join ubicacion
 on c.idUbicacionFacturacion = ubicacion.idUbicacion
 inner join localidad on localidad.idLocalidad = ubicacion.idLocalidad
 inner join provincia on provincia.idProvincia = localidad.idProvincia
SET 
fv.calleFacturacion = ubicacion.calle,
fv.codigoPostalFacturacion = localidad.codigoPostal,
fv.departamentoFacturacion = ubicacion.departamento,
fv.descripcionFacturacion = ubicacion.descripcion,
fv.idLocalidadFacturacion = ubicacion.idLocalidad,
fv.idProvinciaFacturacion = localidad.idProvincia,
fv.idUbicacionFacturacion = ubicacion.idUbicacion,
fv.latitudFacturacion = ubicacion.latitud,
fv.longitudFacturacion = ubicacion.longitud,
fv.nombreLocalidadFacturacion = localidad.nombre,
fv.nombreProvinciaFacturacion = provincia.nombre;

UPDATE
facturaventa fv inner join cliente c on c.id_Cliente = fv.id_Cliente
 inner join ubicacion
 on c.idUbicacionEnvio = ubicacion.idUbicacion
 inner join localidad on localidad.idLocalidad = ubicacion.idLocalidad
 inner join provincia on provincia.idProvincia = localidad.idProvincia
SET 
fv.calleEnvio = ubicacion.calle,
fv.codigoPostalEnvio = localidad.codigoPostal,
fv.departamentoEnvio = ubicacion.departamento,
fv.descripcionEnvio = ubicacion.descripcion,
fv.idLocalidadEnvio = ubicacion.idLocalidad,
fv.idProvinciaEnvio = localidad.idProvincia,
fv.idUbicacionEnvio = ubicacion.idUbicacion,
fv.latitudEnvio = ubicacion.latitud,
fv.longitudEnvio = ubicacion.longitud,
fv.nombreLocalidadEnvio = localidad.nombre,
fv.nombreProvinciaEnvio = provincia.nombre;

SET SQL_SAFE_UPDATES = 1;