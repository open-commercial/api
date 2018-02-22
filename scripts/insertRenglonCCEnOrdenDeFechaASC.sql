-- VENTA
INSERT INTO rengloncuentacorriente (descripcion, eliminado, fecha, fechaVencimiento, idMovimiento, monto, numero, serie, tipo_comprobante, idAjusteCuentaCorriente, idCuentaCorriente,
id_Factura, idNota, idRecibo)
SELECT * FROM
(SELECT "" as descripcion, factura.eliminada as eliminado, fecha, fechaVencimiento, factura.id_Factura as idMovimiento, -total as monto, numFactura as numero, numSerie as serie, factura.tipoComprobante as tipo_comprobante, null as idAjusteCuentaCorriente,
 cuentacorrientecliente.idCuentaCorriente as idCuentaCorriente, factura.id_Factura as id_Factura, null as idNota, null as idRecibo
FROM 
factura inner join facturaventa on factura.id_Factura = facturaventa.id_Factura
inner join cuentacorrientecliente on cuentacorrientecliente.id_Cliente = facturaventa.id_Cliente
UNION ALL
SELECT concepto as descripcion, recibo.eliminado as eliminado, fecha, null, idRecibo as idMovimiento, monto, numRecibo as numero, numSerie as serie, "RECIBO" as tipo_comprobante, null as idAjusteCuentaCorriente, 
cuentacorrientecliente.idCuentaCorriente as idCuentaCorriente, null as id_Factura, null as idNota, idRecibo as idRecibo
from 
recibo inner join cuentacorrientecliente on recibo.id_Cliente = cuentacorrientecliente.id_Cliente
UNION ALL
SELECT motivo as descripcion, eliminada as eliminado, fecha, fecha as fechaVencimiento, idNota as idMovimiento, 
(CASE WHEN (tipoComprobante = "NOTA_CREDITO_A" OR tipoComprobante = "NOTA_CREDITO_B"
OR tipoComprobante = "NOTA_CREDITO_X" OR tipoComprobante = "NOTA_CREDITO_Y"
OR tipoComprobante = "NOTA_CREDITO_PRESUPUESTO") 
THEN total
ELSE -total
END) as monto, 
nroNota as numero, serie as serie, tipoComprobante as tipo_comprobante, null as idAjusteCuentaCorriente, cuentacorrientecliente.idCuentaCorriente as idCuentaCorriente, null, idNota as idNota, null
from nota inner join cuentacorrientecliente on cuentacorrientecliente.id_Cliente = nota.id_Cliente) as A order by A.fecha asc;
-- ALTER TABLE rengloncuentacorriente CHANGE COLUMN idRenglonCuentaCorriente id_renglon_cuenta_corriente BIGINT(20) NOT NULL;
-- ALTER TABLE cuentacorriente CHANGE COLUMN idCuentaCorriente id_cuenta_corriente BIGINT(20) NOT NULL;
-- COMPRA
INSERT INTO rengloncuentacorriente (descripcion, eliminado, fecha, fechaVencimiento, idMovimiento, monto, numero, serie, tipo_comprobante, idAjusteCuentaCorriente, idCuentaCorriente,
id_Factura, idNota, idRecibo)
SELECT * FROM
(SELECT "" as descripcion, factura.eliminada as eliminado, fecha, fechaVencimiento, factura.id_Factura as idMovimiento, -total as monto, numFactura as numero, numSerie as serie, factura.tipoComprobante as tipo_comprobante, null as idAjusteCuentaCorriente,
 cuentacorrienteproveedor.idCuentaCorriente as idCuentaCorriente, factura.id_Factura as id_Factura, null as idNota, null as idRecibo
FROM 
factura inner join facturacompra on factura.id_Factura = facturacompra.id_Factura
inner join cuentacorrienteproveedor on cuentacorrienteproveedor.id_Proveedor = facturacompra.id_Proveedor
UNION ALL
SELECT concepto as descripcion, recibo.eliminado as eliminado, fecha, null, idRecibo as idMovimiento, monto, numRecibo as numero, numSerie as serie, "RECIBO" as tipo_comprobante, null as idAjusteCuentaCorriente, 
cuentacorrienteproveedor.idCuentaCorriente as idCuentaCorriente, null as id_Factura, null as idNota, idRecibo as idRecibo
from 
recibo inner join cuentacorrienteproveedor on recibo.id_Proveedor = cuentacorrienteproveedor.id_Proveedor) as B order by B.fecha asc;
