SELECT renglonfactura.codigoItem, renglonfactura.descripcionItem, medida.nombre as 'medida',
  SUM(renglonfactura.cantidad) AS 'cantidad', SUM(renglonfactura.importe) as 'importe'
FROM renglonfactura inner join facturaventa on renglonfactura.id_Factura = facturaventa.id_Factura
  inner join factura on facturaventa.id_Factura = factura.id_Factura
  inner join producto on renglonfactura.id_ProductoItem = producto.id_Producto
  inner join medida on producto.id_Medida = medida.id_Medida
WHERE factura.eliminada = false
GROUP BY renglonfactura.codigoItem, renglonfactura.descripcionItem, medida.nombre
ORDER BY cantidad desc
limit 0,500;
