SET SQL_SAFE_UPDATES = 0;

update producto , (SELECT idProducto, sum(cantidad)  as sumaCantidad
FROM cantidadensucursal 
GROUP BY idProducto) as sumas
set producto.cantidadTotalEnSucursales = sumas.sumaCantidad
where producto.cantidadTotalEnSucursales is null and producto.idProducto = sumas.idProducto;

update producto
set producto.hayStock = 1
where producto.cantidadTotalEnSucursales > 0.000000000000000;

ALTER TABLE producto DROP COLUMN cantMinima;

ALTER TABLE producto CHANGE bulto cantMinima decimal(25,15);

update producto
set producto.cantMinima = 0.000000000000000
where producto.cantMinima is null;
SET SQL_SAFE_UPDATES = 1;

