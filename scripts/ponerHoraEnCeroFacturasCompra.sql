update factura
inner join facturacompra on factura.id_Factura = facturacompra.id_Factura
set factura.fecha = DATE(factura.fecha);
