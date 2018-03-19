-- NOTA
 INSERT into new_schema.nota(idNota, CAE, eliminada, fecha, iva105Neto, iva21Neto, motivo, nroNota, numNotaAfip, numSerieAfip, serie, subTotalBruto,
 tipoComprobante, total, vencimientoCAE, id_Empresa, id_Usuario) 
 select idNota, CAE, eliminada, fecha, iva105Neto, iva21Neto, motivo, nroNota, numNotaAfip, numSerieAfip, serie, subTotalBruto,
 tipoComprobante, total, vencimientoCAE, id_Empresa, id_Usuario from
 (SELECT * FROM ykcojs0liv7ir9od.nota) as nota;
-- NOTA CREDITO
 INSERT into new_schema.notacredito(descuentoNeto, descuentoPorcentaje, modificaStock, recargoPorcentaje, subTotal, idNota) 
 select descuentoNeto, descuentoPorcentaje, modificaStock, recargoPorcentaje, subTotal, idNota from
 (SELECT * FROM ykcojs0liv7ir9od.notacredito) as notacredito;
-- NOTA CREDITO CLIENTE
 INSERT into new_schema.notacreditocliente(idNota, id_Cliente, id_Factura) 
 select idNota, id_Cliente, id_Factura from
 (SELECT nota.idNota, id_Cliente, id_Factura from nota inner join notacredito on nota.idNota = notacredito.idNota) as notacreditocliente;
-- NOTA DEBITO
 INSERT into new_schema.notadebito(montoNoGravado, idNota, idRecibo) 
 select montoNoGravado, idNota, idRecibo from
 (SELECT * FROM ykcojs0liv7ir9od.notadebito) as notadebito;
 SELECT montoNoGravado, idNota, idRecibo FROM ykcojs0liv7ir9od.notadebito;
-- NOTA DEBITO CLIENTE
 INSERT into new_schema.notadebitocliente(idNota, id_Cliente) 
 select idNota, id_Cliente from
 (SELECT nota.idNota, id_Cliente FROM ykcojs0liv7ir9od.nota inner join notadebito on nota.idNota = notadebito.idNota) as notadebitocliente;