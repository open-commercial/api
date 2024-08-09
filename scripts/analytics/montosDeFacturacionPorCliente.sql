-- Monto de facturas por clientes en un determinado año.
select cliente.nroCliente, cliente.nombreFiscal, cliente.nombreFantasia, SUM(factura.total) as totalFacturas
from factura inner join facturaventa on factura.id_Factura = facturaventa.id_Factura
    inner join cliente on facturaventa.id_Cliente = cliente.id_Cliente
where year(factura.fecha) = "2019" and factura.eliminada = false and cliente.eliminado = false
group by cliente.id_Cliente
order by totalFacturas desc;