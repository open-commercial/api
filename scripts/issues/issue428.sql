SET SQL_SAFE_UPDATES = 0;

DELETE productofavorito

FROM 
productofavorito inner join producto
on productofavorito.idProducto = producto.idProducto

WHERE producto.publico is false;

DELETE productofavorito

FROM 
productofavorito inner join producto
on productofavorito.idProducto = producto.idProducto

WHERE producto.eliminado is true;

SET SQL_SAFE_UPDATES = 1;
