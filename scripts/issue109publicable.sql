ALTER TABLE producto
ADD publico BIT(1) AFTER ilimitado;

SET SQL_SAFE_UPDATES = 0;
UPDATE producto
SET producto.publico = true;
SET SQL_SAFE_UPDATES = 1;