select cliente.razonSocial, cliente.nombreFantasia, count(*) as cantidad_pedidos 
from pedido inner join cliente on pedido.id_Cliente = cliente.id_Cliente
where pedido.id_Empresa = 1
group by cliente.id_Cliente
order by cantidad_pedidos desc;
