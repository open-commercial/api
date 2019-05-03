select renglonpedido.descripcionItem, rubro.nombre, proveedor.razonSocial,
	sum(renglonpedido.cantidad) as cantidadPedida, sum(renglonpedido.importe) as sumImporte
from pedido inner join renglonpedido on pedido.id_Pedido = renglonpedido.id_Pedido
	inner join producto on renglonpedido.idProductoItem = producto.idProducto
    inner join rubro on producto.id_Rubro = rubro.id_Rubro
    inner join proveedor on producto.id_Proveedor = proveedor.id_Proveedor
 where pedido.eliminado = false and pedido.id_Empresa = 1
 group by renglonpedido.descripcionItem, rubro.nombre, proveedor.razonSocial
 order by sumImporte desc
