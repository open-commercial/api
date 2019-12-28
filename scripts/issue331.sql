alter table cliente drop column bonificacion;
alter TABLE cliente ADD COLUMN montoCompraMinima decimal(25,15) after nombreFiscal;
alter TABLE producto ADD COLUMN porcentajeBonificacionPrecio decimal(25,15) default 0 after porcentajeBonificacionOferta;
alter TABLE producto ADD COLUMN precioBonificado decimal(25,15) default 0 after porcentajeBonificacionPrecio;
ALTER TABLE producto
Change porcentajeBonificacionOferta porcentajeBonificacionOferta decimal(25,15) default 0;

SET SQL_SAFE_UPDATES = 0;

update producto
set precioBonificado = producto.precioLista - (producto.precioLista * (producto.porcentajeBonificacionOferta / 100))
where producto.porcentajeBonificacionOferta > 0;

update producto
set precioBonificado = producto.precioLista - (producto.precioLista * (producto.porcentajeBonificacionPrecio / 100))
where producto.porcentajeBonificacionOferta is null;

update producto
set porcentajeBonificacionOferta =  0
where porcentajeBonificacionOferta is null;

update cliente 
set montoCompraMinima = 0;

SET SQL_SAFE_UPDATES = 1;

TRUNCATE TABLE itemcarritocompra; 


