-- SOBRE LA DB CON EL DUMP DE PROD
ALTER TABLE notacredito MODIFY modificaStock bit(1) AFTER descuentoPorcentaje; 
ALTER TABLE notadebito CHANGE column pagoId idRecibo BIGINT(20) NOT NULL AFTER idNota;
ALTER TABLE notadebito ADD pagada bit(1) DEFAULT false after montoNoGravado; 
ALTER TABLE pago ADD idRecibo BIGINT(20) AFTER idNota;
TRUNCATE rengloncuentacorriente;
 
-- SOBRE LA NUEVA ESTRUCTURA CON LOS DATOS
SET SQL_SAFE_UPDATES = 0;
SET foreign_key_checks = 0;
SET UNIQUE_CHECKS = 0; 

UPDATE pago inner join facturaventa on pago.id_Factura = facturaventa.id_Factura SET idRecibo = id_Pago;
UPDATE pago inner join facturacompra on pago.id_Factura = facturacompra.id_Factura SET idRecibo = id_Pago;
UPDATE pago inner join notadebito on pago.idNota = notadebito.idNota SET notadebito.idRecibo = id_Pago;

INSERT INTO recibo (idRecibo, concepto, eliminado, fecha, monto, numRecibo, numSerie, saldoSobrante, id_Cliente, id_Empresa, id_FormaDePago, id_Usuario)
SELECT pago.id_Pago, CONCAT("Recibo por pago Nº: ", nroPago), eliminado, fecha, monto, nroPago, (CASE WHEN id_Empresa = 1 THEN 2 ELSE 0 END), 0, id_Cliente, id_Empresa, id_FormaDePago, id_Usuario
FROM pago inner join facturaventa on pago.id_Factura = facturaventa.id_Factura;

INSERT INTO recibo (idRecibo, concepto, eliminado, fecha, monto, numRecibo, numSerie, saldoSobrante, id_Proveedor, id_Empresa, id_FormaDePago)
SELECT pago.id_Pago, CONCAT("Recibo por pago Nº: ", nroPago), eliminado, fecha, monto, nroPago, (CASE WHEN id_Empresa = 1 THEN 2 ELSE 0 END), 0, id_Proveedor, id_Empresa, id_FormaDePago
FROM pago inner join facturacompra on pago.id_Factura = facturacompra.id_Factura;

INSERT INTO recibo (idRecibo, concepto, eliminado, fecha, monto, numRecibo, numSerie, saldoSobrante, id_Cliente, id_Empresa, id_FormaDePago, id_Usuario)
SELECT pago.id_Pago, CONCAT("Recibo por pago Nº: ", nroPago), eliminado, pago.fecha, pago.monto, nroPago, (CASE WHEN pago.id_Empresa = 1 THEN 2 ELSE 0 END), 0, id_Cliente, pago.id_Empresa, id_FormaDePago, id_Usuario
FROM pago inner join nota on pago.idNota = nota.idNota;


SET SQL_SAFE_UPDATES = 1;
SET foreign_key_checks = 1;
SET UNIQUE_CHECKS = 1; 

-- RenglonesCC
START TRANSACTION;
SET SQL_SAFE_UPDATES=0;

INSERT INTO rengloncuentacorriente (eliminado, fecha, fechaVencimiento, idMovimiento, monto, numero, serie, tipo_comprobante, idCuentaCorriente, id_Factura)
SELECT factura.eliminada, fecha, fechaVencimiento, factura.id_Factura, -total, numFactura, numSerie, tipoComprobante, facturaventa.id_Cliente, factura.id_Factura
FROM 
factura inner join facturaventa on factura.id_Factura = facturaventa.id_Factura
inner join cuentacorriente on facturaventa.id_Cliente = cuentacorriente.id_Cliente;
-- Recibos 
INSERT INTO rengloncuentacorriente (descripcion, eliminado, fecha, idMovimiento, monto, numero, serie, tipo_comprobante, idCuentaCorriente, idRecibo)
SELECT  concepto, eliminado, fecha, idRecibo, monto, numRecibo, numSerie, "RECIBO", id_Cliente, idRecibo
FROM recibo;
-- Nota
INSERT INTO rengloncuentacorriente (descripcion, eliminado, fecha, idMovimiento, monto, numero, serie, tipo_comprobante, idCuentaCorriente, idNota)
SELECT motivo, eliminada, fecha, idNota, 
(CASE WHEN (tipoComprobante = "NOTA_CREDITO_A" OR tipoComprobante = "NOTA_CREDITO_B"
OR tipoComprobante = "NOTA_CREDITO_X" OR tipoComprobante = "NOTA_CREDITO_Y"
OR tipoComprobante = "NOTA_CREDITO_PRESUPUESTO") 
THEN total
ELSE -total
END), 
nroNota, serie, tipoComprobante, id_Cliente, idNota
from nota;

SET SQL_SAFE_UPDATES=1;
COMMIT;
