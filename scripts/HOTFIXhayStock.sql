alter TABLE producto add column hayStock bit(1) default false after cantidadTotalEnSucursales;

SET SQL_SAFE_UPDATES = 0;
update producto 
set hayStock = true
where cantidadTotalEnSucursales > 0;
SET SQL_SAFE_UPDATES = 1;