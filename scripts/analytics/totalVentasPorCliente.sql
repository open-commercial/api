select cliente.razonSocial as razonSocialTablaFacturas, sum(factura.total) as montoFacturado from cliente
inner join facturaventa on facturaventa.id_Cliente = cliente.id_Cliente
inner join factura on factura.id_Factura = facturaventa.id_Factura
where cliente.id_Empresa = 1 and factura.eliminada = false
group by cliente.id_Cliente
order by montoFacturado desc;
