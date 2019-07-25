select cliente.nombreFiscal, cliente.nombreFantasia, count(*) as cantidadPedidos
from pedido inner join cliente on pedido.id_Cliente = cliente.id_Cliente
where pedido.id_Empresa = 1 and pedido.eliminado = false
group by cliente.id_Cliente
order by cantidadPedidos desc;
