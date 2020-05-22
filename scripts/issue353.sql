SET SQL_SAFE_UPDATES = 0;
UPDATE pedido
SET pedido.estado = "CERRADO"
WHERE pedido.estado = "ABIERTO" or pedido.estado = "ACTIVO";
SET SQL_SAFE_UPDATES = 1;

ALTER TABLE configuracionsucursal
ADD predeterminada bit(1) after puntoDeRetiro;

UPDATE configuracionsucursal
SET predeterminada = 1
WHERE configuracionsucursal.idSucursal = 1;
