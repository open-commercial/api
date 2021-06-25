SET SQL_SAFE_UPDATES = 0;

UPDATE producto
SET cantMinima = 0.000000000000000
WHERE cantMinima is null;

UPDATE producto
SET cantidadTotalEnSucursales = 0.000000000000000
WHERE cantidadTotalEnSucursales is null;

SET SQL_SAFE_UPDATES = 1;