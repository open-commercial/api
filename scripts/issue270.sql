ALTER TABLE proveedor
ADD nroProveedor varchar(255) not null;

SET SQL_SAFE_UPDATES = 0;

update proveedor
set nroProveedor = truncate(RAND(proveedor.id_Proveedor) * 100000, 0);

SET SQL_SAFE_UPDATES = 1;

ALTER TABLE proveedor
drop codigo;


