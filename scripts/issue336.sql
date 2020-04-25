ALTER TABLE producto
ADD version bigint(11) default 1 after urlImagen;
ALTER TABLE cantidadensucursal
ADD version bigint(11) default 1 after estante;
