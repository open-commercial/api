ALTER TABLE `factura` 
ADD COLUMN `cantidadArticulos` decimal(25,15) NOT NULL AFTER `observaciones`;

UPDATE factura f,(select factura.id_Factura, sum(renglonfactura.cantidad) as suma from factura
inner join renglonfactura on factura.id_Factura = renglonfactura.id_Factura
group by factura.id_Factura) as s
   SET f.cantidadArticulos = s.suma
  WHERE f.id_Factura = s.id_Factura