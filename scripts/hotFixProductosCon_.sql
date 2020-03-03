-- 1ra parte, para eliminar los productos de la sucursal minorista
SET SQL_SAFE_UPDATES = 0;
UPDATE producto
SET producto.eliminado = true
WHERE LENGTH((left (producto.descripcion, locate('_', producto.descripcion) - 1))) > 0 ;
SET SQL_SAFE_UPDATES = 1;

-- 2da parte, para aplicar un porcentaje de bonificacion precio a los productos con bonificacion 0
SET SQL_SAFE_UPDATES = 0;
UPDATE producto
SET producto.porcentajeBonificacionPrecio = "20.000000000000000", producto.bulto = 2
WHERE producto.eliminado = false and producto.porcentajeBonificacionPrecio = "0.000000000000000";

UPDATE producto
SET producto.precioBonificado = producto.precioLista - ((producto.precioLista * producto.porcentajeBonificacionPrecio)  / 100)
WHERE producto.porcentajeBonificacionPrecio =  "20.000000000000000" and producto.oferta = false;
SET SQL_SAFE_UPDATES = 1;
