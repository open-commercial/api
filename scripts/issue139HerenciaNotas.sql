ALTER TABLE nota
ADD id_Cliente bigint(20),
ADD id_Proveedor bigint(20),
ADD id_FacturaVenta bigint(20),
ADD id_FacturaCompra bigint(20),
ADD movimiento varchar(255);

SET SQL_SAFE_UPDATES = 0;
  
UPDATE nota AS n
INNER JOIN notacreditocliente AS nc ON n.idNota = nc.idNota
SET n.id_Cliente = nc.id_Cliente,
  n.id_FacturaVenta = nc.id_Factura,
  n.movimiento = "VENTA";

UPDATE nota AS n
INNER JOIN notacreditoproveedor AS nc ON n.idNota = nc.idNota
SET n.id_Proveedor = nc.id_Proveedor,
  n.id_FacturaCompra = nc.id_Factura,
  n.movimiento = "COMPRA";
  
  UPDATE nota AS n
INNER JOIN notadebitocliente AS nd ON n.idNota = nd.idNota
SET n.id_Cliente = nd.id_Cliente,
  n.movimiento = "VENTA";
  
  UPDATE nota AS n
INNER JOIN notadebitoproveedor AS nd ON n.idNota = nd.idNota
SET n.id_Cliente = nd.id_Proveedor,
  n.movimiento = "COMPRA";

SET SQL_SAFE_UPDATES = 1;

DROP TABLE notacreditocliente, notacreditoproveedor, notadebitocliente, notadebitoproveedor;

