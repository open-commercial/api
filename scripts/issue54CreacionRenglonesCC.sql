START TRANSACTION;
SET SQL_SAFE_UPDATES=0;
-- CUENTAS CORRIENTES
-- INSERT INTO cuentacorriente
-- SELECT id_Cliente, eliminado, fechaAlta, id_Cliente, id_Empresa 
-- FROM cliente;
-- FACTURAS 
INSERT INTO rengloncuentacorriente (eliminado, fecha, fechaVencimiento, idMovimiento, monto, numero, serie, tipoComprobante, idCuentaCorriente, id_Factura)
SELECT factura.eliminada, fecha, fechaVencimiento, factura.id_Factura, total, numFactura, numSerie, tipoComprobante, facturaventa.id_Cliente, factura.id_Factura
FROM 
factura inner join facturaventa on factura.id_Factura = facturaventa.id_Factura
inner join cuentacorriente on facturaventa.id_Cliente = cuentacorriente.id_Cliente;
-- Recibos 
INSERT INTO rengloncuentacorriente (descripcion, eliminado, fecha, idMovimiento, monto, numero, serie, tipoComprobante, idCuentaCorriente, idRecibo)
SELECT  concepto, eliminado, fecha, idRecibo, monto, numRecibo, numSerie, "RECIBO", id_Cliente, idRecibo
FROM recibo;
-- Nota
INSERT INTO rengloncuentacorriente (descripcion, eliminado, fecha, idMovimiento, monto, numero, serie, tipoComprobante, idCuentaCorriente, idNota)
SELECT motivo, eliminada, fecha, idNota, total, nroNota, serie, tipoComprobante, id_Cliente, idNota
from nota;

SET SQL_SAFE_UPDATES=1;
COMMIT;