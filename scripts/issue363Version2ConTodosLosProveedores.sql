SELECT proveedor.razonSocial, producto.codigo, producto.descripcion, medida.nombre AS 'medida', SUM(renglonpedido.cantidad) AS 'cant', SUM(renglonpedido.cantidad * producto.precioLista) AS 'monto'
FROM pedido INNER JOIN renglonpedido ON pedido.id_Pedido = renglonpedido.id_Pedido
	INNER JOIN producto ON producto.id_Producto = renglonpedido.id_Producto
	INNER JOIN proveedor ON proveedor.id_Proveedor = producto.id_Proveedor
    INNER JOIN medida ON medida.id_Medida = producto.id_Medida
GROUP BY proveedor.razonSocial, producto.codigo, producto.descripcion, medida.nombre
ORDER BY cant DESC
