SET SQL_SAFE_UPDATES = 0;
UPDATE pedido
SET pedido.estado = "CERRADO"
WHERE pedido.estado = "ABIERTO" or pedido.estado = "ACTIVO";
SET SQL_SAFE_UPDATES = 1;
