SET sql_mode = '';
SELECT sic.renglonfactura.codigoItem, sic.renglonfactura.descripcionItem, sic.medida.nombre as 'Unidad de Medida',
SUM(sic.renglonfactura.cantidad) AS 'cantidad', SUM(sic.renglonfactura.importe) as 'importe'
FROM sic.renglonfactura inner join sic.facturaventa on sic.renglonfactura.id_Factura = sic.facturaventa.id_Factura 
inner join sic.factura on sic.facturaventa.id_Factura = sic.factura.id_Factura
inner join sic.producto on sic.renglonfactura.id_ProductoItem = sic.producto.id_Producto
inner join sic.medida on sic.producto.id_Medida = sic.medida.id_Medida
where sic.factura.eliminada = false
GROUP BY sic.renglonfactura.codigoItem
order by cantidad desc
limit 0,500;
