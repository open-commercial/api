select cliente.nombreFiscal, cliente.nombreFantasia,
	sum(factura.total) as montoFacturado,
    count(factura.id_Factura) as cantidadFacturas
from cliente
	inner join facturaventa on facturaventa.id_Cliente = cliente.id_Cliente
	inner join factura on factura.id_Factura = facturaventa.id_Factura
where factura.eliminada = false and cliente.eliminado = false
group by cliente.id_Cliente
order by montoFacturado desc;
