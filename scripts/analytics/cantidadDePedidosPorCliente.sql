-- Cantidad de pedidos por cliente, agrupados por años y meses. 
SELECT year(pedido.fecha) as Año, monthname(pedido.fecha) as Mes, cliente.nombreFantasia, cliente.nombreFiscal, cliente.nroCliente,
 usuario.nombre nombreViajante, concat(ubicacion.calle, " ", ubicacion.numero) as CalleYNumero, localidad.nombre as Localidad, provincia.nombre as Provincia,
 count(*) cantidadDePedidos
FROM pedido inner join cliente on pedido.id_Cliente = cliente.id_Cliente

left join usuario on cliente.id_Usuario_Viajante = usuario.id_Usuario

inner join ubicacion on cliente.idUbicacionFacturacion = ubicacion.idUbicacion
inner join localidad on ubicacion.idLocalidad = localidad.idLocalidad
inner join provincia on provincia.idProvincia = localidad.idProvincia

where year(pedido.fecha) < 2020  
and pedido.eliminado = false and cliente.eliminado = false
and pedido.idSucursal = 1
group by cliente.id_Cliente, year(pedido.fecha), monthname(pedido.fecha)
order by cantidadDePedidos desc;