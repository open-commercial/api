START TRANSACTION;
SET SQL_SAFE_UPDATES=0;
UPDATE sic.producto
SET 
ganancia_neto= precioCosto * (ganancia_porcentaje / 100),
precioVentaPublico= precioCosto + ganancia_neto,
iva_neto= precioVentaPublico * (iva_porcentaje / 100),
impuestoInterno_neto = precioVentaPublico * (impuestoInterno_porcentaje / 100),
precioLista = precioVentaPublico + iva_neto + impuestoInterno_neto;
SET SQL_SAFE_UPDATES=1;
COMMIT;