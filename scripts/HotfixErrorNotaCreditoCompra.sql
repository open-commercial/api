
-- Consultas para verificar el estado del stock antes de aplicar los cambios y después para su comparación

SELECT renglonnotacredito.idProductoItem, renglonnotacredito.codigoItem, renglonnotacredito.descripcionItem, sum(renglonnotacredito.cantidad) as cantidadNota,
producto.cantidadTotalEnSucursales 
FROM nota inner join notacredito on nota.idNota = notacredito.idNota
INNER JOIN renglonnotacredito on renglonnotacredito.idNota = nota.idNota
inner join producto on producto.idProducto = renglonnotacredito.idProductoItem
where nota.movimiento = "COMPRA" and notacredito.modificaStock = 1 and date(nota.fecha) > CONVERT_TZ('2020-07-02 00:00:00','-03:00','+00:00') and nota.idSucursal = 5
group by renglonnotacredito.idProductoItem, renglonnotacredito.codigoItem, renglonnotacredito.descripcionItem;

SELECT renglonnotacredito.idProductoItem, renglonnotacredito.codigoItem, renglonnotacredito.descripcionItem, sum(renglonnotacredito.cantidad) as cantidadNota,
cantidadensucursal.idProducto, cantidadensucursal.cantidad as cantidadEnSucursalMayorista
FROM nota inner join notacredito on nota.idNota = notacredito.idNota
INNER JOIN renglonnotacredito on renglonnotacredito.idNota = nota.idNota
inner join cantidadensucursal on cantidadensucursal.idProducto = renglonnotacredito.idProductoItem
where nota.movimiento = "COMPRA" and notacredito.modificaStock = 1 and date(nota.fecha) > CONVERT_TZ('2020-07-02 00:00:00','-03:00','+00:00') and nota.idSucursal = 5
and cantidadensucursal.idSucursal = 1
group by renglonnotacredito.idProductoItem, renglonnotacredito.codigoItem, renglonnotacredito.descripcionItem, cantidadensucursal.idProducto, cantidadensucursal.cantidad;

SELECT renglonnotacredito.idProductoItem, renglonnotacredito.codigoItem, renglonnotacredito.descripcionItem, sum(renglonnotacredito.cantidad) as cantidadNota,
cantidadensucursal.idProducto, cantidadensucursal.cantidad as cantidadEnSucursalMinorista
FROM nota inner join notacredito on nota.idNota = notacredito.idNota
INNER JOIN renglonnotacredito on renglonnotacredito.idNota = nota.idNota
inner join cantidadensucursal on cantidadensucursal.idProducto = renglonnotacredito.idProductoItem
where nota.movimiento = "COMPRA" and notacredito.modificaStock = 1 and date(nota.fecha) > CONVERT_TZ('2020-07-02 00:00:00','-03:00','+00:00') and nota.idSucursal = 5
and cantidadensucursal.idSucursal = 5
group by renglonnotacredito.idProductoItem, renglonnotacredito.codigoItem, renglonnotacredito.descripcionItem, cantidadensucursal.idProducto, cantidadensucursal.cantidad;

-----------

SELECT renglonnotacredito.idProductoItem, renglonnotacredito.codigoItem, renglonnotacredito.descripcionItem, sum(renglonnotacredito.cantidad) as cantidadNota,
producto.cantidadTotalEnSucursales 
FROM nota inner join notacredito on nota.idNota = notacredito.idNota
INNER JOIN renglonnotacredito on renglonnotacredito.idNota = nota.idNota
inner join producto on producto.idProducto = renglonnotacredito.idProductoItem
where nota.movimiento = "COMPRA" and notacredito.modificaStock = 1 and date(nota.fecha) > CONVERT_TZ('2020-07-02 00:00:00','-03:00','+00:00') and nota.idSucursal = 1
group by renglonnotacredito.idProductoItem, renglonnotacredito.codigoItem, renglonnotacredito.descripcionItem;


SELECT renglonnotacredito.idProductoItem, renglonnotacredito.codigoItem, renglonnotacredito.descripcionItem, sum(renglonnotacredito.cantidad) as cantidadNota,
cantidadensucursal.idProducto, cantidadensucursal.cantidad as cantidadEnSucursalMayorista
FROM nota inner join notacredito on nota.idNota = notacredito.idNota
INNER JOIN renglonnotacredito on renglonnotacredito.idNota = nota.idNota
inner join cantidadensucursal on cantidadensucursal.idProducto = renglonnotacredito.idProductoItem
where nota.movimiento = "COMPRA" and notacredito.modificaStock = 1 and date(nota.fecha) > CONVERT_TZ('2020-07-02 00:00:00','-03:00','+00:00') and nota.idSucursal = 1
and cantidadensucursal.idSucursal = 1
group by renglonnotacredito.idProductoItem, renglonnotacredito.codigoItem, renglonnotacredito.descripcionItem, cantidadensucursal.idProducto, cantidadensucursal.cantidad;

SELECT renglonnotacredito.idProductoItem, renglonnotacredito.codigoItem, renglonnotacredito.descripcionItem, sum(renglonnotacredito.cantidad) as cantidadNota,
cantidadensucursal.idProducto, cantidadensucursal.cantidad as cantidadEnSucursalMinorista
FROM nota inner join notacredito on nota.idNota = notacredito.idNota
INNER JOIN renglonnotacredito on renglonnotacredito.idNota = nota.idNota
inner join cantidadensucursal on cantidadensucursal.idProducto = renglonnotacredito.idProductoItem
where nota.movimiento = "COMPRA" and notacredito.modificaStock = 1 and date(nota.fecha) > CONVERT_TZ('2020-07-02 00:00:00','-03:00','+00:00') and nota.idSucursal = 1
and cantidadensucursal.idSucursal = 5
group by renglonnotacredito.idProductoItem, renglonnotacredito.codigoItem, renglonnotacredito.descripcionItem, cantidadensucursal.idProducto, cantidadensucursal.cantidad;

-- Modifica el stock debido al error en notas de credito compra. 

UPDATE producto AS b1, ( SELECT idProductoItem as id, codigoItem as codigo, sum(cantidad) as cantidadT FROM nota inner join notacredito on nota.idNota = notacredito.idNota
INNER JOIN renglonnotacredito on renglonnotacredito.idNota = nota.idNota
where nota.movimiento = "COMPRA" and notacredito.modificaStock = 1 and date(nota.fecha) > CONVERT_TZ('2020-07-02 00:00:00','-03:00','+00:00') and nota.idSucursal = 5
group by idProductoItem, codigoItem) AS b2
SET b1.cantidadTotalEnSucursales = b1.cantidadTotalEnSucursales - b2.cantidadT
WHERE b1.idProducto = b2.id;

UPDATE producto AS b1, ( SELECT idProductoItem as id, codigoItem as codigo, sum(cantidad) as cantidadT FROM nota inner join notacredito on nota.idNota = notacredito.idNota
INNER JOIN renglonnotacredito on renglonnotacredito.idNota = nota.idNota
where nota.movimiento = "COMPRA" and notacredito.modificaStock = 1 and date(nota.fecha) > CONVERT_TZ('2020-07-02 00:00:00','-03:00','+00:00') and nota.idSucursal = 1
group by idProductoItem, codigoItem) AS b2
SET b1.cantidadTotalEnSucursales = b1.cantidadTotalEnSucursales - b2.cantidadT
WHERE b1.idProducto = b2.id;

UPDATE cantidadensucursal AS b1, ( SELECT idProductoItem as id, codigoItem as codigo, sum(cantidad) as cantidadT FROM nota inner join notacredito on nota.idNota = notacredito.idNota
INNER JOIN renglonnotacredito on renglonnotacredito.idNota = nota.idNota
where nota.movimiento = "COMPRA" and notacredito.modificaStock = 1 and date(nota.fecha) > CONVERT_TZ('2020-07-02 00:00:00','-03:00','+00:00') and nota.idSucursal = 5
group by idProductoItem, codigoItem) AS b2
SET b1.cantidad = b1.cantidad- b2.cantidadT
WHERE b1.idProducto= b2.id and b1.idSucursal = 5;

UPDATE cantidadensucursal AS b1, ( SELECT idProductoItem as id, codigoItem as codigo, sum(cantidad) as cantidadT FROM nota inner join notacredito on nota.idNota = notacredito.idNota
INNER JOIN renglonnotacredito on renglonnotacredito.idNota = nota.idNota
where nota.movimiento = "COMPRA" and notacredito.modificaStock = 1 and date(nota.fecha) > CONVERT_TZ('2020-07-02 00:00:00','-03:00','+00:00') and nota.idSucursal = 1
group by idProductoItem, codigoItem) AS b2
SET b1.cantidad = b1.cantidad- b2.cantidadT
WHERE b1.idProducto= b2.id and b1.idSucursal = 1;