SET SQL_SAFE_UPDATES = 0;
alter table producto add column bulto decimal(25,15) after cantidad;
update producto
set producto.bulto = 1;
alter table producto drop column ventaMinima;
SET SQL_SAFE_UPDATES = 1;