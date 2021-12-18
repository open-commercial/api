INSERT INTO provincia(nombre)
VALUES ('Corrientes'),('Misiones'),('Chaco');

INSERT INTO localidad(codigo_postal, costo_envio, envio_gratuito, nombre, id_provincia)
VALUES ('N3400',50.000000000000000,false,'Corrientes',1),
       ('W3470',300.000000000000000,false,'Mercedes',1),
       ('N3300',800.000000000000000,false,'Posadas',2),
       ('H3500',150.000000000000000,false,'Resistencia',3);

INSERT INTO formadepago(afecta_caja, eliminada, nombre, predeterminado)
VALUES (true,false,'Efectivo',true), (false, false, 'Cheque de 3ros', false);

INSERT INTO usuario (apellido, eliminado, email, habilitado, id_sucursal_predeterminada, nombre,
    password, password_recovery_key, password_recovery_key_expiration_date, username)
VALUES ('test',false,'test@test.com',true,1,'Usuario Test','098f6bcd4621d373cade4e832627b4f6','',NULL,'test');

INSERT INTO rol (id_usuario, nombre)
VALUES (1,'ADMINISTRADOR');


