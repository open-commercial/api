select proveedor.razonSocial,
	sum(factura.total) as montoFacturado,
    count(factura.id_Factura) as cantidadFacturas
from proveedor
	inner join facturacompra on facturacompra.id_Proveedor = proveedor.id_Proveedor
	inner join factura on factura.id_Factura = facturacompra.id_Factura
where factura.eliminada = false and proveedor.eliminado = false
group by proveedor.id_Proveedor
order by montoFacturado desc;
