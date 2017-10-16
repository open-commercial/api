SELECT proveedor.razonSocial, SUM(renglonpedido.cantidad) AS 'cant', SUM(renglonpedido.subTotal) AS 'monto'
FROM pedido INNER JOIN renglonpedido ON pedido.id_Pedido = renglonpedido.id_Pedido
	INNER JOIN producto ON producto.id_Producto = renglonpedido.id_Producto
	INNER JOIN proveedor ON proveedor.id_Proveedor = producto.id_Proveedor    
GROUP BY proveedor.razonSocial
ORDER BY monto DESC
