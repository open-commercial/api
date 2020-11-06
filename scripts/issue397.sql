ALTER TABLE remito CHANGE `totalFactura` `totalFacturas` decimal(25,15);

ALTER TABLE remito DROP COLUMN tipoComprobante;

SET @row := 0;
update remito
set remito.nroRemito = (@row := @row + 1)
where remito.idSucursal = 1;

SET @row := 0;
update remito
set remito.nroRemito = (@row := @row + 1)
where remito.idSucursal = 5;

