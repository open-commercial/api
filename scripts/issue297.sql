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
from producto;

CREATE TABLE productoAux (
  idProducto bigint(20),
  cantidad decimal(25,15) DEFAULT NULL,
  codigo varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  estante varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  estanteria varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  id_Empresa bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

insert into productoAux(idProducto, cantidad, codigo, estante, estanteria, id_Empresa)
select producto.idProducto, producto.cantidad, producto.codigo, producto.estante, producto.estante, producto.id_Empresa from producto
where producto.id_Empresa = 1;

INSERT INTO cantidadensucursal(cantidad, estante, estanteria, id_Empresa, idProducto)
SELECT 
   productoAux.cantidad, productoAux.estante, productoAux.estanteria, productoAux.id_Empresa, cantidadensucursal.idProducto
   from cantidadensucursal inner join
 productoAux on cantidadensucursal.codigo = productoAux.codigo;
 
ALTER TABLE cantidadensucursal DROP codigo;
 
DROP TABLE IF EXISTS productoAux; 
 
ALTER TABLE producto DROP cantMinima;
ALTER TABLE producto DROP estante;
ALTER TABLE producto DROP estanteria;
 
ALTER TABLE producto
	DROP FOREIGN KEY FKmicsquyd17liutvxtw6uao7fo;
    
ALTER TABLE producto DROP id_Empresa;
 