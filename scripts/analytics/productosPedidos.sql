select renglonpedido.descripcionItem as 'descripcion',
    rubro.nombre as 'rubro',
    proveedor.razonSocial as 'proveedor',
	sum(renglonpedido.cantidad) as cantidadPedida,
    sum(renglonpedido.importe) as sumImporte,
    producto.precioCosto as precioCostoDelProducto
from pedido inner join renglonpedido on pedido.id_Pedido = renglonpedido.id_Pedido
	inner join producto on renglonpedido.idProductoItem = producto.idProducto
    inner join rubro on producto.id_Rubro = rubro.id_Rubro
    inner join proveedor on producto.id_Proveedor = proveedor.id_Proveedor
 where pedido.eliminado = false and pedido.idSucursal = 1
 group by renglonpedido.descripcionItem, rubro.nombre, proveedor.razonSocial, producto.precioCosto
 order by cantidadPedida desc

-- Enviar en un mismo file, con un tab para cada sucursal
