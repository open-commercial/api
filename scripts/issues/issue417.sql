ALTER TABLE recibo
ADD estado varchar(255) AFTER idPagoMercadoPago;

ALTER TABLE recibo
ADD urlImagen varchar(255) AFTER estado;

SET SQL_SAFE_UPDATES = 0;

update recibo
set recibo.estado = 'APROBADO';

SET SQL_SAFE_UPDATES = 1;

