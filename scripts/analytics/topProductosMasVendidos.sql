SELECT renglonfactura.codigoItem, renglonfactura.descripcionItem, medida.nombre as 'medida',
SUM(renglonfactura.cantidad) AS 'cantidad', SUM(renglonfactura.importe) as 'importe'

FROM factura INNER JOIN facturaventa ON factura.id_Factura = facturaventa.id_Factura
INNER JOIN renglonfactura ON renglonfactura.id_Factura = facturaventa.id_Factura
INNER JOIN producto ON producto.idProducto = renglonfactura.id_ProductoItem
INNER JOIN medida ON medida.id_Medida = producto.id_Medida

WHERE factura.eliminada = false AND factura.id_Empresa = 1
GROUP BY renglonfactura.codigoItem, renglonfactura.descripcionItem, medida.nombre
ORDER BY cantidad desc
LIMIT 0,2000;
