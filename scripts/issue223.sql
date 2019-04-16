ALTER TABLE `factura` 
ADD COLUMN `cantidadArticulos` decimal(25,15) NOT NULL AFTER `observaciones`;

UPDATE factura f,(select factura.id_Factura, sum(renglonfactura.cantidad) as suma from factura
inner join renglonfactura on factura.id_Factura = renglonfactura.id_Factura
group by factura.id_Factura) as s
   SET f.cantidadArticulos = s.suma
  WHERE f.id_Factura = s.id_Factura;
  
ALTER TABLE `pedido` 
ADD COLUMN `cantidadArticulos` decimal(25,15) NOT NULL AFTER `estado`;

UPDATE pedido p,(select pedido.id_Pedido, sum(renglonpedido.cantidad) as suma from pedido
inner join renglonpedido on pedido.id_Pedido = renglonpedido.id_Pedido
group by pedido.id_Pedido) as s
   SET p.cantidadArticulos = s.suma
  WHERE p.id_Pedido = s.id_Pedido;