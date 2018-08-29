select razonSocialTablaFacturas, TRUNCATE(monto, 2) AS MONTO, cantPedidos 
from (select cliente.id_cliente as idClienteTablaFacturas, cliente.razonSocial as razonSocialTablaFacturas, sum(factura.total) as monto from cliente
inner join facturaventa on facturaventa.id_Cliente = cliente.id_Cliente
inner join factura on factura.id_Factura = facturaventa.id_Factura
where cliente.id_Empresa = 1 and factura.eliminada = false
group by cliente.id_Cliente) as tablaFacturas 
inner join (select cliente.id_Cliente as idClienteTablaPedidos, cliente.razonSocial as razonSocialTablaPedidos, count(*) as cantPedidos
from pedido
inner join cliente on pedido.id_Cliente = cliente.id_Cliente
where cliente.id_Empresa = 1 and pedido.eliminado = false
group by cliente.id_Cliente) as tablaPedidos
on tablaFacturas.idClienteTablaFacturas = tablaPedidos.idClienteTablaPedidos;