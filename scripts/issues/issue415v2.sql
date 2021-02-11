ALTER TABLE producto
ADD cantidadReservada decimal(25,15) DEFAULT 0 AFTER cantidadTotalEnSucursales; 
   
SELECT renglonpedido.idProductoItem as id, sum(renglonpedido.cantidad) as cantidadReservada from pedido inner join renglonpedido 
on pedido.id_Pedido = renglonpedido.id_Pedido 
WHERE pedido.estado = "ABIERTO"
group by renglonpedido.idProductoItem;    

SET SQL_SAFE_UPDATES = 0;
UPDATE
    producto AS t
    LEFT JOIN (
SELECT renglonpedido.idProductoItem as id, sum(renglonpedido.cantidad) as cantidadReservada from pedido inner join renglonpedido 
on pedido.id_Pedido = renglonpedido.id_Pedido 
WHERE pedido.estado = "ABIERTO"
group by renglonpedido.idProductoItem
    ) AS m ON
        m.id = t.idProducto
SET
    t.cantidadReservada = m.cantidadReservada;
SET SQL_SAFE_UPDATES = 1;