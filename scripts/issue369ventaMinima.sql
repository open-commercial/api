use sic;
UPDATE producto 
SET producto.ventaMinima = 1 
WHERE producto.ventaMinima = 0;
