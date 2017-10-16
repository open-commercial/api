START TRANSACTION;
USE sic;
SET SQL_SAFE_UPDATES=0;
UPDATE (factura INNER JOIN renglonfactura ON factura.id_Factura = renglonfactura.id_Factura) 
INNER JOIN facturacompra ON factura.id_Factura = facturacompra.id_Factura
SET iva_neto = 0
WHERE factura.tipoComprobante = "FACTURA_X"; 
SET SQL_SAFE_UPDATES=1;
COMMIT;
