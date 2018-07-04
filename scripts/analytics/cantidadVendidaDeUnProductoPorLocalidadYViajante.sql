
SELECT renglonfactura.codigoItem, renglonfactura.descripcionItem, sum(renglonfactura.cantidad) as CantidadVendida, 
provincia.nombre as Provincia, localidad.nombre as Localidad, usuario.nombre as NombreViajante, usuario.apellido as ApellidoViajante
FROM facturaventa 
inner join factura on factura.id_Factura = facturaventa.id_Factura
inner join renglonfactura on renglonfactura.id_Factura = facturaventa.id_Factura
inner join cliente on facturaventa.id_Cliente = cliente.id_Cliente
inner join usuario on cliente.id_Usuario_Viajante = usuario.id_Usuario
inner join localidad on localidad.id_Localidad = cliente.id_Localidad
inner join provincia on localidad.id_Provincia = provincia.id_Provincia
where factura.fecha between "2018-05-01" and "2018-06-29" and renglonfactura.codigoItem = "DLV.PVC.25.BCE"
group by usuario.id_Usuario, localidad.id_Localidad, renglonfactura.descripcionItem
order by cantidadVendida DESC;
