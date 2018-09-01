ALTER TABLE producto
ADD gananciaNeto decimal(25,15) after ganancia_neto,
ADD gananciaPorcentaje decimal(25,15) after ganancia_porcentaje,
ADD impuestoInternoNeto decimal(25,15) after impuestoInterno_neto,
ADD impuestoInternoPorcentaje decimal(25,15) after impuestoInterno_porcentaje,
ADD ivaNeto decimal(25,15) after iva_neto,
ADD ivaPorcentaje decimal(25,15) after iva_porcentaje;

SET SQL_SAFE_UPDATES = 0;
UPDATE producto
SET gananciaNeto = ganancia_neto,
gananciaPorcentaje = ganancia_porcentaje,
impuestoInternoNeto  = impuestoInterno_neto,
impuestoInternoPorcentaje = impuestoInterno_porcentaje,
ivaNeto = iva_neto,
ivaPorcentaje = iva_porcentaje;
SET SQL_SAFE_UPDATES = 1;

ALTER TABLE producto 
DROP ganancia_neto,
DROP ganancia_porcentaje,
DROP impuestoInterno_neto,
DROP impuestoInterno_porcentaje,
DROP iva_neto,
DROP iva_porcentaje;