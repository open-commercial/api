SET sql_mode = '';
SELECT sic.rubro.nombre, -- sic.renglonfactura.descripcionItem,
SUM(sic.renglonpedido.cantidad) AS 'cantidad', SUM(sic.renglonpedido.importe) as 'importe'
FROM sic.renglonpedido 
inner join sic.pedido on sic.pedido.id_Pedido = sic.renglonpedido.id_Factura 
-- inner join sic.factura on sic.facturaventa.id_Factura = sic.factura.id_Factura
-- inner join sic.producto on sic.renglonfactura.id_ProductoItem = sic.producto.id_Producto
inner join sic.rubro on sic.producto.id_Rubro = sic.rubro.id_Rubro
-- inner join sic.medida on sic.producto.id_Medida = sic.medida.id_Medida
where sic.factura.eliminada = false
-- AND sic.factura.fecha >= CAST('2017-07-24' AS DATE)
-- AND sic.factura.fecha <= CAST('2014-07-26' AS DATE)
GROUP BY sic.rubro.id_Rubro
order by cantidad desc
limit 0,500;
