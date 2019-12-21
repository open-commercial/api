alter table cliente drop column bonificacion;
alter TABLE cliente ADD COLUMN montoCompraMinima decimal(25,15) after nombreFiscal;
alter TABLE producto ADD COLUMN porcentajeBonificacionPrecio decimal(25,15) default 0 after porcentajeBonificacionOferta;
alter TABLE producto ADD COLUMN precioBonificado decimal(25,15) default 0 after porcentajeBonificacionPrecio;

update producto
set precioBonificado = producto.precioLista;