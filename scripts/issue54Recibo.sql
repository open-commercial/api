-- SOBRE LA DB CON EL DUMP DE PROD
SET SQL_SAFE_UPDATES = 0;
SET foreign_key_checks = 0;
SET UNIQUE_CHECKS = 0; 

ALTER TABLE notacredito MODIFY modificaStock bit(1) AFTER descuentoPorcentaje; 
ALTER TABLE notadebito CHANGE column pagoId idRecibo BIGINT(20) NOT NULL AFTER idNota;
ALTER TABLE notadebito ADD pagada bit(1) DEFAULT false after montoNoGravado; 

Delete p.* 
from pago p inner join facturacompra fc on p.id_Factura = fc.id_Factura
inner join factura f on fc.id_Factura = f.id_Factura 
where f.pagada = true;

INSERT INTO pago(eliminado, fecha, monto, nota, nroPago, id_Empresa, id_Factura, id_FormaDePago, idNota)
select eliminada, factura.fecha, factura.total, factura.observaciones, 0, factura.id_Empresa, factura.id_Factura, 10, null
from factura inner join facturacompra on factura.id_Factura = facturacompra.id_Factura
where factura.pagada = true;

SET SQL_SAFE_UPDATES = 0;
set @i = (SELECT max(pago.nroPago) FROM pago);
update pago set pago.nroPago=(@i:=@i+1) where pago.nroPago = 0;


ALTER TABLE pago ADD idRecibo BIGINT(20) AFTER idNota;
TRUNCATE rengloncuentacorriente;
TRUNCATE cuentacorriente;

SET SQL_SAFE_UPDATES = 1;
SET foreign_key_checks = 1;
SET UNIQUE_CHECKS = 1; 
 
-- SOBRE LA NUEVA ESTRUCTURA CON LOS DATOS
SET SQL_SAFE_UPDATES = 0;
SET foreign_key_checks = 0;
SET UNIQUE_CHECKS = 0; 

UPDATE pago inner join facturaventa on pago.id_Factura = facturaventa.id_Factura SET idRecibo = id_Pago;
UPDATE pago inner join facturacompra on pago.id_Factura = facturacompra.id_Factura SET idRecibo = id_Pago;
UPDATE pago inner join notadebito on pago.idNota = notadebito.idNota SET notadebito.idRecibo = id_Pago;

INSERT INTO recibo (idRecibo, concepto, eliminado, fecha, monto, numRecibo, numSerie, saldoSobrante, id_Cliente, id_Empresa, id_FormaDePago, id_Usuario)
SELECT pago.id_Pago, CONCAT("Recibo por pago Nº: ", nroPago), eliminado, fecha, pago.monto, nroPago, (CASE WHEN id_Empresa = 1 THEN 2 ELSE 0 END), 0, id_Cliente, id_Empresa, id_FormaDePago, id_Usuario
FROM pago inner join facturaventa on pago.id_Factura = facturaventa.id_Factura;

-- Con usuario para el backfill
insert into usuario(id_Usuario, eliminado, nombre, password) 
values (31, true, "backfillCC", "697416b772e7e3780507ab813ebaae7a");

INSERT INTO recibo (idRecibo, concepto, eliminado, fecha, monto, numRecibo, numSerie, saldoSobrante, id_Proveedor, id_Empresa, id_FormaDePago, id_Usuario)
SELECT pago.id_Pago, CONCAT("Recibo por pago Nº: ", nroPago), eliminado, fecha, pago.monto, nroPago, (CASE WHEN id_Empresa = 1 THEN 2 ELSE 0 END), 0, id_Proveedor, id_Empresa, id_FormaDePago, 31
FROM pago inner join facturacompra on pago.id_Factura = facturacompra.id_Factura;

INSERT INTO recibo (idRecibo, concepto, eliminado, fecha, monto, numRecibo, numSerie, saldoSobrante, id_Cliente, id_Empresa, id_FormaDePago, id_Usuario)
SELECT pago.id_Pago, CONCAT("Recibo por pago Nº: ", nroPago), eliminado, pago.fecha, pago.monto, nroPago, (CASE WHEN pago.id_Empresa = 1 THEN 2 ELSE 0 END), 0, id_Cliente, pago.id_Empresa, id_FormaDePago, id_Usuario
FROM pago inner join nota on pago.idNota = nota.idNota;


SET SQL_SAFE_UPDATES = 1;
SET foreign_key_checks = 1;
SET UNIQUE_CHECKS = 1; 


-- CC

SET SQL_SAFE_UPDATES = 0;
SET AUTOCOMMIT = 0;
SET foreign_key_checks = 0;
SET UNIQUE_CHECKS = 0; 

set @i := 0;

INSERT INTO cuentacorriente (idCuentaCorriente, eliminada , fechaApertura, id_Empresa)
SELECT (@i:=@i+1), eliminado, fechaAlta, id_Empresa 
FROM cliente order by cliente.id_Cliente asc;
-- 
set @j := 0;

INSERT INTO cuentacorrientecliente (idCuentaCorriente, id_Cliente)
SELECT (@j := @j+1), id_Cliente
FROM cliente order by cliente.id_Cliente asc;

set @k := (SELECT max(idCuentaCorriente) FROM cuentacorriente);
set @m := (SELECT max(idCuentaCorriente) FROM cuentacorriente);

INSERT INTO cuentacorriente (idCuentaCorriente, eliminada , fechaApertura, id_Empresa)
SELECT (@k := @k+1), eliminado, NOW(), id_Empresa 
FROM proveedor order by proveedor.id_Proveedor asc;
-- 
INSERT INTO cuentacorrienteproveedor (idCuentaCorriente, id_Proveedor)
SELECT (@m := @m+1), id_Proveedor
FROM proveedor order by proveedor.id_Proveedor asc;
-- --
 
SET SQL_SAFE_UPDATES = 1;
SET AUTOCOMMIT = 1;
SET foreign_key_checks = 1;
SET UNIQUE_CHECKS = 1;


-- RenglonesCC
START TRANSACTION;
SET SQL_SAFE_UPDATES=0;

-- FACTURAS VENTA
INSERT INTO rengloncuentacorriente (eliminado, fecha, fechaVencimiento, idMovimiento, monto, numero, serie, tipo_comprobante, idCuentaCorriente, id_Factura)
SELECT factura.eliminada, fecha, fechaVencimiento, factura.id_Factura, -total, numFactura, numSerie, factura.tipoComprobante, cuentacorrientecliente.idCuentaCorriente, factura.id_Factura
FROM 
factura inner join facturaventa on factura.id_Factura = facturaventa.id_Factura
inner join cuentacorrientecliente on cuentacorrientecliente.id_Cliente = facturaventa.id_Cliente;
-- FACTURAS COMPRA
INSERT INTO rengloncuentacorriente (eliminado, fecha, fechaVencimiento, idMovimiento, monto, numero, serie, tipo_comprobante, idCuentaCorriente, id_Factura)
SELECT factura.eliminada, fecha, fechaVencimiento, factura.id_Factura, -total, numFactura, numSerie, factura.tipoComprobante, cuentacorrienteproveedor.idCuentaCorriente, factura.id_Factura
FROM 
factura inner join facturacompra on factura.id_Factura = facturacompra.id_Factura
inner join cuentacorrienteproveedor on cuentacorrienteproveedor.id_Proveedor = facturacompra.id_Proveedor;

-- Recibos CLIENTE
INSERT INTO rengloncuentacorriente (descripcion, eliminado, fecha, idMovimiento, monto, numero, serie, tipo_comprobante, idCuentaCorriente, idRecibo)
SELECT  concepto, recibo.eliminado, fecha, idRecibo, monto, numRecibo, numSerie, "RECIBO", cuentacorrientecliente.idCuentaCorriente, idRecibo
from 
recibo inner join cuentacorrientecliente on recibo.id_Cliente = cuentacorrientecliente.id_Cliente;

-- Recibos PROVEEDOR
INSERT INTO rengloncuentacorriente (descripcion, eliminado, fecha, idMovimiento, monto, numero, serie, tipo_comprobante, idCuentaCorriente, idRecibo)
SELECT  concepto, recibo.eliminado, fecha, idRecibo, monto, numRecibo, numSerie, "RECIBO", cuentacorrienteproveedor.idCuentaCorriente, idRecibo
from 
recibo inner join cuentacorrienteproveedor on recibo.id_Proveedor = cuentacorrienteproveedor.id_Proveedor;

-- Nota
INSERT INTO rengloncuentacorriente (descripcion, eliminado, fecha, fechaVencimiento, idMovimiento, monto, numero, serie, tipo_comprobante, idAjusteCuentaCorriente, idCuentaCorriente,
id_Factura, idNota, idRecibo)
SELECT motivo, eliminada, fecha, fecha, idNota, 
(CASE WHEN (tipoComprobante = "NOTA_CREDITO_A" OR tipoComprobante = "NOTA_CREDITO_B"
OR tipoComprobante = "NOTA_CREDITO_X" OR tipoComprobante = "NOTA_CREDITO_Y"
OR tipoComprobante = "NOTA_CREDITO_PRESUPUESTO") 
THEN total
ELSE -total
END), 
nroNota, serie, tipoComprobante, null, cuentacorrientecliente.idCuentaCorriente, null, idNota, null
from nota inner join cuentacorrientecliente on cuentacorrientecliente.id_Cliente = nota.id_Cliente;

SET SQL_SAFE_UPDATES=1;
COMMIT;
