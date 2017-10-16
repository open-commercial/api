START TRANSACTION;
SET SQL_SAFE_UPDATES=0;
-- CUENTAS CORRIENTES
INSERT INTO cuentacorriente
SELECT id_Cliente, eliminado, fechaAlta, id_Cliente, id_Empresa 
FROM cliente;
-- FACTURAS 
INSERT INTO rengloncuentacorriente (comprobante, eliminado, fecha, fechaVencimiento, idMovimiento, monto, tipoMovimiento, id_Factura, idCuentaCorriente)
SELECT  CONCAT( 
CASE tipoComprobante
WHEN "FACTURA_A" THEN "FACTURA \"A\"" 
WHEN "FACTURA_B" THEN "FACTURA \"B\"" 
WHEN "FACTURA_C" THEN "FACTURA \"C\"" 
WHEN "FACTURA_X" THEN "FACTURA \"X\"" 
WHEN "FACTURA_Y" THEN "FACTURA \"Y\"" 
WHEN "PRESUPUESTO" THEN "PRESUPUESTO" 
ELSE NULL 
END
," ", numSerie," - ",numFactura), factura.eliminada, fecha, fechaVencimiento, factura.id_Factura, -total, "VENTA", factura.id_Factura, idCuentaCorriente
FROM 
factura inner join facturaventa on factura.id_Factura = facturaventa.id_Factura
inner join cuentacorriente on facturaventa.id_Cliente = cuentacorriente.id_Cliente;
-- PAGOS 
INSERT INTO rengloncuentacorriente (comprobante, descripcion, eliminado, fecha, idMovimiento, monto, tipoMovimiento, id_Pago, idCuentaCorriente)
SELECT  CONCAT("PAGO Nº ", pago.nroPago), nota , pago.eliminado, pago.fecha, pago.id_Pago, pago.monto, "PAGO", pago.id_Pago, idCuentaCorriente
FROM 
factura inner join pago on factura.id_Factura = pago.id_Factura
inner join facturaventa on factura.id_Factura = facturaventa.id_Factura
inner join cuentacorriente on facturaventa.id_Cliente = cuentacorriente.id_Cliente;

SET SQL_SAFE_UPDATES=1;
COMMIT;