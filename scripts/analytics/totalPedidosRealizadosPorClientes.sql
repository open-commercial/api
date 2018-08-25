SELECT cliente.razonSocial, count(*) as cantidad_pedidos FROM pedido
inner join cliente on pedido.id_Cliente = cliente.id_Cliente
where pedido.id_Empresa = 1
group by cliente.id_Cliente
order by cantidad_pedidos desc;