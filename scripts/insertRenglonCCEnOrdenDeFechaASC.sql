-- ANTES DEL EXPORT DATA
ALTER TABLE `ykcojs0liv7ir9od`.`rengloncuentacorriente`
DROP FOREIGN KEY `FK9o2j89cigpiqo83vfc38hlmj7`;

ALTER TABLE `ykcojs0liv7ir9od`.`rengloncuentacorriente`
CHANGE COLUMN `idRenglonCuentaCorriente` `id_renglon_cuenta_corriente` BIGINT(20) NOT NULL AUTO_INCREMENT ,
CHANGE COLUMN `idCuentaCorriente` `id_cuenta_corriente` BIGINT(20) NULL DEFAULT NULL ;

ALTER TABLE `ykcojs0liv7ir9od`.`rengloncuentacorriente`
ADD CONSTRAINT `FK9o2j89cigpiqo83vfc38hlmj7`
  FOREIGN KEY (`id_cuenta_corriente`)
  REFERENCES `ykcojs0liv7ir9od`.`cuentacorriente` (`idCuentaCorriente`);

ALTER TABLE `ykcojs0liv7ir9od`.`cuentacorrientecliente`
DROP FOREIGN KEY `FK1a9mncn9lp8prvon5vg1p77q1`;
ALTER TABLE `ykcojs0liv7ir9od`.`cuentacorrientecliente`
CHANGE COLUMN `idCuentaCorriente` `id_cuenta_corriente` BIGINT(20) NOT NULL ;

ALTER TABLE `ykcojs0liv7ir9od`.`cuentacorrientecliente`
ADD CONSTRAINT `FK1a9mncn9lp8prvon5vg1p77q1`
  FOREIGN KEY (`id_cuenta_corriente`)
  REFERENCES `ykcojs0liv7ir9od`.`cuentacorriente` (`idCuentaCorriente`);

ALTER TABLE `ykcojs0liv7ir9od`.`cuentacorrienteproveedor`
DROP FOREIGN KEY `FK1etk6qygtsymfy2d1tv06mh8t`;

ALTER TABLE `ykcojs0liv7ir9od`.`cuentacorrienteproveedor`
CHANGE COLUMN `idCuentaCorriente` `id_cuenta_corriente` BIGINT(20) NOT NULL ;

ALTER TABLE `ykcojs0liv7ir9od`.`cuentacorrienteproveedor`
ADD CONSTRAINT `FK1etk6qygtsymfy2d1tv06mh8t`
  FOREIGN KEY (`id_cuenta_corriente`)
  REFERENCES `ykcojs0liv7ir9od`.`cuentacorriente` (`idCuentaCorriente`);

ALTER TABLE `ykcojs0liv7ir9od`.`cuentacorriente`
CHANGE COLUMN `idCuentaCorriente` `id_cuenta_corriente` BIGINT(20) NOT NULL AUTO_INCREMENT ;

-- ---------------------------------------------------------------------------------------------------------------------

-- CC CLIENTES
INSERT INTO rengloncuentacorriente (descripcion, eliminado, fecha, fechaVencimiento, idMovimiento, monto, numero, serie,
 tipo_comprobante, id_cuenta_corriente, id_Factura, idNota, idRecibo)
SELECT * FROM
(SELECT "" as descripcion, factura.eliminada as eliminado, fecha, fechaVencimiento, factura.id_Factura as idMovimiento, -total as monto,
 numFactura as numero, numSerie as serie, factura.tipoComprobante as tipo_comprobante,
 cuentacorrientecliente.id_cuenta_corriente as id_cuenta_corriente, factura.id_Factura as id_Factura, null as idNota, null as idRecibo
FROM factura inner join facturaventa on factura.id_Factura = facturaventa.id_Factura
	inner join cuentacorrientecliente on cuentacorrientecliente.id_Cliente = facturaventa.id_Cliente
UNION ALL
	SELECT concepto as descripcion, recibo.eliminado as eliminado, fecha, null, idRecibo as idMovimiento, monto, numRecibo as numero, numSerie as serie,
    "RECIBO" as tipo_comprobante, cuentacorrientecliente.id_cuenta_corriente as id_cuenta_corriente, null as id_Factura, null as idNota, idRecibo as idRecibo
	FROM recibo inner join cuentacorrientecliente on recibo.id_Cliente = cuentacorrientecliente.id_Cliente
UNION ALL
	SELECT motivo as descripcion, eliminada as eliminado, fecha, fecha as fechaVencimiento, idNota as idMovimiento,
	(CASE WHEN (tipoComprobante = "NOTA_CREDITO_A" OR tipoComprobante = "NOTA_CREDITO_B"
		OR tipoComprobante = "NOTA_CREDITO_X" OR tipoComprobante = "NOTA_CREDITO_Y" OR tipoComprobante = "NOTA_CREDITO_PRESUPUESTO")
		THEN total ELSE -total END) as monto,
		nroNota as numero, serie as serie, tipoComprobante as tipo_comprobante,
		cuentacorrientecliente.id_cuenta_corriente as id_cuenta_corriente, null, idNota as idNota, null
	FROM nota inner join cuentacorrientecliente on cuentacorrientecliente.id_Cliente = nota.id_Cliente) as A
    ORDER BY A.fecha ASC;
-- CC PROVEEDORES
INSERT INTO rengloncuentacorriente (descripcion, eliminado, fecha, fechaVencimiento, idMovimiento, monto, numero, serie, tipo_comprobante,
 id_cuenta_corriente, id_Factura, idNota, idRecibo)
SELECT * FROM
	(SELECT "" as descripcion, factura.eliminada as eliminado, fecha, fechaVencimiento, factura.id_Factura as idMovimiento, -total as monto, numFactura as numero,
    numSerie as serie, factura.tipoComprobante as tipo_comprobante,
	cuentacorrienteproveedor.id_cuenta_corriente as id_cuenta_corriente, factura.id_Factura as id_Factura, null as idNota, null as idRecibo
	FROM factura inner join facturacompra on factura.id_Factura = facturacompra.id_Factura
	inner join cuentacorrienteproveedor on cuentacorrienteproveedor.id_Proveedor = facturacompra.id_Proveedor
	UNION ALL
		SELECT concepto as descripcion, recibo.eliminado as eliminado, fecha, null, idRecibo as idMovimiento, monto, numRecibo as numero,
        numSerie as serie, "RECIBO" as tipo_comprobante,
		cuentacorrienteproveedor.id_cuenta_corriente as id_cuenta_corriente, null as id_Factura, null as idNota, idRecibo as idRecibo
		FROM recibo inner join cuentacorrienteproveedor on recibo.id_Proveedor = cuentacorrienteproveedor.id_Proveedor) as B
        ORDER BY B.fecha ASC;
