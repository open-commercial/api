-- SOBRE LA DB CON EL DUMP DE PROD
-- ALTER TABLE notacredito MODIFY modificaStock bit(1) AFTER descuentoPorcentaje; 
-- ALTER TABLE notadebito CHANGE column pagoId idRecibo BIGINT(20) NOT NULL AFTER idNota;
-- ALTER TABLE notadebito ADD pagada bit(1) DEFAULT false after montoNoGravado; 
-- ALTER TABLE pago ADD idRecibo BIGINT(20) AFTER idNota;
-- TRUNCATE rengloncuentacorriente;

-- SOBRE LA NUEVA ESTRUCTURA CON LOS DATOS
SET SQL_SAFE_UPDATES = 0;
SET foreign_key_checks = 0;
SET UNIQUE_CHECKS = 0; 
SET SQL_SAFE_UPDATES = 0;
SET foreign_key_checks = 0;
SET UNIQUE_CHECKS = 0; 

UPDATE pago SET idRecibo = id_Pago;

INSERT INTO recibo
SELECT id_Pago, CONCAT("Recibo por pago Nº: ", nroPago), eliminado, fecha, monto, nroPago, 2, 0, id_Cliente, id_Empresa, id_FormaDePago, id_Usuario
FROM pago inner join facturaventa where pago.id_Factura = facturaventa.id_Factura;


SET SQL_SAFE_UPDATES = 1;
SET foreign_key_checks = 1;
SET UNIQUE_CHECKS = 1; 