alter table cliente drop column bonificacion;
alter TABLE cliente ADD COLUMN montoCompraMinima decimal(25,15) after nombreFiscal;
alter TABLE producto ADD COLUMN porcentajeBonificacionPrecio decimal(25,15) default 0 after porcentajeBonificacionOferta;
alter TABLE producto ADD COLUMN precioBonificado decimal(25,15) default 0 after porcentajeBonificacionPrecio;

SET SQL_SAFE_UPDATES = 0;
update producto
set precioBonificado = producto.precioLista - (producto.precioLista * (producto.porcentajeBonificacionOferta / 100))
where producto.porcentajeBonificacionOferta > 0;

update cliente 
set montoCompraMinima = 1000000;

SET SQL_SAFE_UPDATES = 1;

