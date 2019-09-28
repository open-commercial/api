CREATE TABLE cantidadensucursal (
  idCantidadEnSucursal bigint(20) NOT NULL AUTO_INCREMENT,
  cantidad decimal(25,15) DEFAULT NULL,
  estante varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  estanteria varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  id_Empresa bigint(20) NOT NULL,
  idProducto bigint(20) DEFAULT NULL,
  PRIMARY KEY (idCantidadEnSucursal),
  KEY FKah2gat74y707din7l3k4lqd0d (id_Empresa),
  KEY FKlbd386vgya8ugkt0ynp67k8wl (idProducto),
  CONSTRAINT FKah2gat74y707din7l3k4lqd0d FOREIGN KEY (id_Empresa) REFERENCES empresa (id_Empresa),
  CONSTRAINT FKlbd386vgya8ugkt0ynp67k8wl FOREIGN KEY (idProducto) REFERENCES producto (idProducto)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

UPDATE producto SET eliminado = true where id_Empresa = 5;

insert into cantidadensucursal(cantidad, estante, estanteria, id_Empresa, idProducto)
select producto.cantidad, producto.estante, producto.estanteria, 1, producto.idProducto
from producto where producto.eliminado = false;

insert into cantidadensucursal(cantidad, estante, estanteria, id_Empresa, idProducto)
select 0, "", "", 5, producto.idProducto
from producto where producto.eliminado = false;
 
ALTER TABLE producto DROP estante;
ALTER TABLE producto DROP estanteria;
 
ALTER TABLE producto
	DROP FOREIGN KEY FKmicsquyd17liutvxtw6uao7fo;
    
ALTER TABLE producto DROP id_Empresa;

alter TABLE rubro DROP FOREIGN KEY  FKjqodxje0wqn40nptfj4sij5al;
alter TABLE rubro drop column id_Empresa;
UPDATE producto SET id_Rubro = 15 where id_Rubro = 32;
UPDATE producto SET id_Rubro = 3 where id_Rubro = 34;
UPDATE producto SET id_Rubro = 1 where id_Rubro = 37;
UPDATE producto SET id_Rubro = 4 where id_Rubro = 39;
UPDATE producto SET id_Rubro = 7 where id_Rubro = 41;
UPDATE producto SET id_Rubro = 5 where id_Rubro = 53;
UPDATE producto SET id_Rubro = 10 where id_Rubro = 68;
UPDATE producto SET id_Rubro = 11 where id_Rubro = 69;
UPDATE producto SET id_Rubro = 8 where id_Rubro = 70;
UPDATE producto SET id_Rubro = 12 where id_Rubro = 71;
UPDATE producto SET id_Rubro = 6 where id_Rubro = 72;
UPDATE producto SET id_Rubro = 9 where id_Rubro = 74;

alter TABLE medida DROP FOREIGN KEY FK5jsf5bmdsydn5wfvlgsofl4vf;
alter TABLE medida drop column id_Empresa;
UPDATE producto SET id_Medida = 1 where id_Medida = 18;
UPDATE producto SET id_Medida = 6 where id_Medida = 19;


DELETE FROM medida WHERE id_Medida = 18; 
DELETE FROM medida WHERE id_Medida = 19;

alter TABLE transportista DROP FOREIGN KEY FKphhgo5taxw9nhjkav8ei6b6y9;
alter TABLE transportista drop column id_Empresa;

update cliente 
set cliente.predeterminado = false
where cliente.id_Empresa = 5;
alter TABLE cliente DROP FOREIGN KEY FKahu5l6761ite2fsglie24w1bg;
alter TABLE cliente drop column id_Empresa;

alter TABLE proveedor DROP FOREIGN KEY FK5s5a4d2763thtum39ht6r059q;
alter TABLE proveedor drop column id_Empresa;

alter TABLE cuentacorriente DROP FOREIGN KEY FKs7jnro4dgqdaexbg57371xkr2;
alter TABLE cuentacorriente drop column id_Empresa;

RENAME TABLE empresa TO sucursal;

ALTER TABLE sucursal CHANGE id_Empresa idSucursal bigint(20) NOT NULL AUTO_INCREMENT;

ALTER TABLE caja CHANGE id_Empresa idSucursal bigint(20) NOT NULL;
ALTER TABLE cantidadensucursal CHANGE id_Empresa idSucursal bigint(20) NOT NULL;
ALTER TABLE configuraciondelsistema CHANGE id_Empresa idSucursal bigint(20) NOT NULL;
ALTER TABLE factura CHANGE id_Empresa idSucursal bigint(20) NOT NULL;
ALTER TABLE gasto CHANGE id_Empresa idSucursal bigint(20) NOT NULL;
ALTER TABLE nota CHANGE id_Empresa idSucursal bigint(20) NOT NULL;
ALTER TABLE pedido CHANGE id_Empresa idSucursal bigint(20) NOT NULL;
ALTER TABLE recibo CHANGE id_Empresa idSucursal bigint(20) NOT NULL;
ALTER TABLE usuario CHANGE idEmpresaPredeterminada idSucursalPredeterminada bigint(20) NOT NULL;

alter TABLE configuraciondelsistema add column puntoDeRetiro bit(1) default false after emailSenderHabilitado;

RENAME TABLE configuraciondelsistema TO configuracionsucursal;

ALTER TABLE configuracionsucursal CHANGE id_ConfiguracionDelSistema idConfiguracionSucursal bigint(20) NOT NULL AUTO_INCREMENT;

alter TABLE configuracionsucursal drop column emailPassword;
alter TABLE configuracionsucursal drop column emailSenderHabilitado;
alter TABLE configuracionsucursal drop column emailUsername;

alter TABLE renglonfactura CHANGE descuentoPorcentaje bonificacionPorcentaje decimal(25,15);
alter TABLE renglonfactura CHANGE descuentoNeto bonificacionNeta decimal(25,15);

alter TABLE renglonpedido CHANGE descuentoPorcentaje bonificacionPorcentaje decimal(25,15);
alter TABLE renglonpedido CHANGE descuentoNeto bonificacionNeta decimal(25,15);
alter TABLE renglonpedido add importeAnterior decimal(25,15) after descuentoNeto;

alter TABLE producto CHANGE destacado oferta bit(1);
alter TABLE producto ADD COLUMN bonificacionoferta decimal(25,15) after oferta;