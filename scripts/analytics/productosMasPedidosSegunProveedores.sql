-- Genera una lista ordenada por cantidad de productos pedidos y sus proveedores
SELECT proveedor.razonSocial, renglonpedido.idProductoItem, producto.descripcion, count(renglonpedido.cantidad) as cantidadPedida
 FROM producto inner join renglonpedido on producto.idProducto = renglonpedido.idProductoItem
 inner join pedido on pedido.id_Pedido = renglonpedido.id_Pedido
inner join proveedor on proveedor.id_Proveedor = producto.id_Proveedor
where pedido.eliminado = false and producto.eliminado = false
	AND (pedido.fecha >= CONVERT_TZ('2019-11-01 00:00:00','-03:00','+00:00')
	    AND pedido.fecha <= CONVERT_TZ('2019-11-30 23:59:59','-03:00','+00:00'))
group by proveedor.razonSocial, renglonpedido.idProductoItem
order by cantidadPedida desc;
