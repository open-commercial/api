alter table cliente drop column bonificacion;
alter TABLE cliente ADD COLUMN montoCompraMinima decimal(25,15) after nombreFiscal;
alter TABLE producto ADD COLUMN porcentajeBonificacionPrecio decimal(25,15) after porcentajeBonificacionOferta;