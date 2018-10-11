-- SELECT * FROM factura;
ALTER TABLE cliente add column bonificacion Decimal(25,15);
ALTER TABLE factura CHANGE descuento_neto descuentoNeto Decimal(25,15);
ALTER TABLE factura CHANGE descuento_porcentaje descuentoPorcentaje Decimal(25,15);
ALTER TABLE factura CHANGE impuestoInterno_neto impuestoInternoNeto Decimal(25,15);
ALTER TABLE factura CHANGE iva_105_neto iva105Neto Decimal(25,15);
ALTER TABLE factura CHANGE iva_21_neto iva21Neto Decimal(25,15);
ALTER TABLE factura CHANGE recargo_neto recargoNeto Decimal(25,15);
ALTER TABLE factura CHANGE recargo_porcentaje recargoPorcentaje Decimal(25,15);
ALTER TABLE factura CHANGE subTotal_bruto subTotalBruto Decimal(25,15);
ALTER TABLE producto DROP COLUMN producto.impuestoInternoNeto;
ALTER TABLE producto DROP COLUMN producto.impuestoInternoPorcentaje;
ALTER TABLE renglonfactura CHANGE descuento_neto descuentoNeto Decimal(25,15);
ALTER TABLE renglonfactura CHANGE descuento_porcentaje descuentoPorcentaje Decimal(25,15);
ALTER TABLE renglonfactura CHANGE ganancia_neto gananciaNeto Decimal(25,15);
ALTER TABLE renglonfactura CHANGE ganancia_porcentaje gananciaPorcentaje Decimal(25,15);
ALTER TABLE renglonfactura CHANGE iva_neto ivaNeto Decimal(25,15);
ALTER TABLE renglonfactura CHANGE iva_porcentaje ivaPorcentaje Decimal(25,15);
ALTER TABLE renglonfactura DROP COLUMN renglonfactura.impuesto_neto;
ALTER TABLE renglonfactura DROP COLUMN renglonfactura.impuesto_porcentaje;
ALTER TABLE renglonfactura CHANGE id_ProductoItem idProductoItem bigint(20);
-- renglon pedido
ALTER TABLE renglonpedido CHANGE descuento_porcentaje descuentoPorcentaje Decimal(25,15);
ALTER TABLE renglonpedido CHANGE descuento_neto descuentoNeto Decimal(25,15);
ALTER TABLE renglonpedido add column idProductoItem bigint(20);
ALTER TABLE renglonpedido add column codigoItem varchar(255);
ALTER TABLE renglonpedido add column descripcionItem varchar(255);
ALTER TABLE renglonpedido add column medidaItem varchar(255);
ALTER TABLE renglonpedido add column precioUnitario Decimal(25,15);
ALTER TABLE renglonpedido add column precioDeLista Decimal(25,15);

SET SQL_SAFE_UPDATES=0;
update producto 
inner join renglonpedido on producto.id_Producto = renglonpedido.id_Producto
inner join medida on producto.id_Medida = medida.id_Medida
set
renglonpedido.idProductoItem = producto.id_Producto,
renglonpedido.codigoItem = producto.codigo,
renglonpedido.descripcionItem = producto.descripcion,
renglonpedido.medidaItem = medida.nombre,
renglonpedido.precioUnitario = renglonpedido.subTotal / renglonpedido.cantidad
where renglonpedido.cantidad > 0;  -- Buscar los comprobantes con cantidades negativas y corregirlos
SET SQL_SAFE_UPDATES=1;

ALTER TABLE renglonpedido DROP foreign key FKtjjxjf88fwccfduk8hhf7q3pd;
ALTER TABLE renglonpedido DROP COLUMN renglonpedido.id_Pedido;
