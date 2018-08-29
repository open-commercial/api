select cliente.razonSocial, cliente.nombreFantasia, (factura.total) as monto_facturado 
from factura inner join facturaventa on factura.id_Factura = facturaventa.id_Factura
inner join cliente on facturaventa.id_Cliente = cliente.id_Cliente
where factura.id_Empresa = 1
group by cliente.id_Cliente
order by monto_facturado desc;
