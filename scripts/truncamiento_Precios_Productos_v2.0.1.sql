START TRANSACTION;
SET SQL_SAFE_UPDATES=0;
UPDATE sic.producto
SET precioCosto=TRUNCATE(precioCosto,2),
ganancia_porcentaje=TRUNCATE(ganancia_porcentaje, 2),
ganancia_neto=TRUNCATE(ganancia_neto, 2),
precioVentaPublico=TRUNCATE(precioVentaPublico, 2),
iva_porcentaje=TRUNCATE(iva_porcentaje, 2),
iva_neto=TRUNCATE(iva_neto, 2),
impuestoInterno_porcentaje=TRUNCATE(impuestoInterno_porcentaje, 2),
impuestoInterno_neto=TRUNCATE(impuestoInterno_neto, 2),
precioLista=TRUNCATE(precioLista, 2);
SET SQL_SAFE_UPDATES=1;
COMMIT;
