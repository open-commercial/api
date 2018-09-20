select MONTH(pedido.fecha) as Mes_2017, sum(pedido.totalEstimado) as Total 
from pedido
where YEAR(pedido.fecha) = "2017" and pedido.id_Empresa = 1 and pedido.eliminado is not false
group by MONTH(pedido.fecha);

select MONTH(pedido.fecha) as Mes_2018, sum(pedido.totalEstimado) as Total 
from pedido
where YEAR(pedido.fecha) = "2018" and pedido.id_Empresa = 1 and pedido.eliminado is not false
group by MONTH(pedido.fecha);
