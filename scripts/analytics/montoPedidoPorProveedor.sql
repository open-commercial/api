select proveedor.razonSocial, sum(renglonpedido.importe) as totalPedido
from renglonpedido inner join producto on renglonpedido.idProductoItem = producto.idProducto
	inner join proveedor on producto.id_Proveedor = proveedor.id_Proveedor
where proveedor.eliminado = false
group by proveedor.id_Proveedor
order by totalPedido desc

-- Cuanto $$ pidieron los clientes nuestros de cada proveedor
