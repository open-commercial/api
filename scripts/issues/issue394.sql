ALTER TABLE facturacompra
ADD COLUMN fechaAlta DATETIME NOT NULL DEFAULT '2020-01-01 00:00:00';

SET SQL_SAFE_UPDATES = 0;
UPDATE facturacompra inner join factura on facturacompra.id_Factura = factura.id_Factura
SET fechaAlta = factura.fecha;
SET SQL_SAFE_UPDATES = 1;

