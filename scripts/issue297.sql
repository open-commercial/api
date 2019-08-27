CREATE TABLE cantidadensucursal (
  idCantidadSucursal bigint(20) NOT NULL AUTO_INCREMENT,
  cantidad decimal(25,15) DEFAULT NULL,
  estante varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  estanteria varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  id_Empresa bigint(20) NOT NULL,
  idProducto bigint(20) DEFAULT NULL,
  codigo varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (idCantidadSucursal),
  KEY FKah2gat74y707din7l3k4lqd0d (id_Empresa),
  KEY FKlbd386vgya8ugkt0ynp67k8wl (idProducto),
  CONSTRAINT FKah2gat74y707din7l3k4lqd0d FOREIGN KEY (id_Empresa) REFERENCES empresa (id_Empresa),
  CONSTRAINT FKlbd386vgya8ugkt0ynp67k8wl FOREIGN KEY (idProducto) REFERENCES producto (idProducto)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

insert into cantidadensucursal(cantidad, estante, estanteria, id_Empresa, idProducto, codigo)
select producto.cantidad, producto.estante, producto.estanteria, producto.id_Empresa, producto.idProducto, producto.codigo
from producto; -- where producto.id_Empresa = 5;
 
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

RENAME TABLE empresa TO sucursal;

ALTER TABLE sucursal CHANGE id_Empresa idSucursal bigint(20) NOT NULL AUTO_INCREMENT;

ALTER TABLE caja CHANGE id_Empresa idSucursal bigint(20) NOT NULL;
ALTER TABLE cantidadensucursal CHANGE id_Empresa idSucursal bigint(20) NOT NULL;
ALTER TABLE cliente CHANGE id_Empresa idSucursal bigint(20) NOT NULL;
ALTER TABLE configuraciondelsistema CHANGE id_Empresa idSucursal bigint(20) NOT NULL;
ALTER TABLE cuentacorriente CHANGE id_Empresa idSucursal bigint(20) NOT NULL;
ALTER TABLE factura CHANGE id_Empresa idSucursal bigint(20) NOT NULL;
ALTER TABLE gasto CHANGE id_Empresa idSucursal bigint(20) NOT NULL;
ALTER TABLE nota CHANGE id_Empresa idSucursal bigint(20) NOT NULL;
ALTER TABLE pedido CHANGE id_Empresa idSucursal bigint(20) NOT NULL;
ALTER TABLE proveedor CHANGE id_Empresa idSucursal bigint(20) NOT NULL;
ALTER TABLE recibo CHANGE id_Empresa idSucursal bigint(20) NOT NULL;
ALTER TABLE transportista CHANGE id_Empresa idSucursal bigint(20) NOT NULL;
ALTER TABLE usuario CHANGE idEmpresaPredeterminada idSucursalPredeterminada bigint(20) NOT NULL;

 