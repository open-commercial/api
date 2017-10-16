SELECT producto.descripcion, SUM(renglonpedido.cantidad) AS 'cant', SUM(renglonpedido.cantidad * producto.precioLista) AS 'monto'
FROM pedido INNER JOIN renglonpedido ON pedido.id_Pedido = renglonpedido.id_Pedido
	INNER JOIN producto ON producto.id_Producto = renglonpedido.id_Producto
	INNER JOIN proveedor ON proveedor.id_Proveedor = producto.id_Proveedor
WHERE proveedor.id_Proveedor=61
GROUP BY producto.descripcion
ORDER BY monto DESC
