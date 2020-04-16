ALTER TABLE producto
ADD version int(11) default 1 after urlImagen;
ALTER TABLE cantidadensucursal
ADD version int(11) default 1 after estante;
