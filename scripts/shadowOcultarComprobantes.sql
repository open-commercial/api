ALTER TABLE factura add column shadow bit(1);
ALTER TABLE nota add column shadow bit(1);

SET SQL_SAFE_UPDATES=0;

update factura
set
factura.shadow = factura.eliminada;
update nota
set
nota.shadow = nota.eliminada;

update factura
set
factura.eliminada = true
where factura.tipoComprobante = "FACTURA_X"
or factura.tipoComprobante = "FACTURA_Y"
or factura.tipoComprobante = "PRESUPUESTO";

update nota
set
nota.eliminada = true
where nota.tipoComprobante = "NOTA_CREDITO_X"
or nota.tipoComprobante = "NOTA_CREDITO_Y"
or nota.tipoComprobante = "NOTA_CREDITO_PRESUPUESTO"
or nota.tipoComprobante = "NOTA_DEBITO_X"
or nota.tipoComprobante = "NOTA_DEBITO_Y"
or nota.tipoComprobante = "NOTA_DEBITO_PRESUPUESTO";

SET SQL_SAFE_UPDATES=1;