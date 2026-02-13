SELECT renglonfactura.codigoItem, renglonfactura.descripcionItem, medida.nombre as 'medida',
SUM(renglonfactura.cantidad) AS 'cantidad', 
ROUND(SUM(renglonfactura.importe),2) as 'importe'
  
FROM factura INNER JOIN facturaventa ON factura.id_Factura = facturaventa.id_Factura
INNER JOIN renglonfactura ON renglonfactura.id_Factura = facturaventa.id_Factura
INNER JOIN producto ON producto.idProducto = renglonfactura.idProductoItem
INNER JOIN medida ON medida.id_Medida = producto.id_Medida
  
WHERE factura.eliminada = false AND factura.idSucursal = 1
GROUP BY renglonfactura.codigoItem, renglonfactura.descripcionItem, medida.nombre
ORDER BY importe desc
