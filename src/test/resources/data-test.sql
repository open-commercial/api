INSERT INTO provincia(nombre) VALUES ('Corrientes'),('Misiones'),('Chaco');

INSERT INTO localidad(codigo_postal, costo_envio, envio_gratuito, nombre, id_provincia)
VALUES ('N3400',0.000000000000000,false,'Corrientes',1),
       ('W3470',0.000000000000000,false,'Mercedes',1),
       ('N3300',0.000000000000000,false,'Posadas',2),
       ('H3500',0.000000000000000,false,'Resistencia',3);

INSERT INTO ubicacion (calle, departamento, descripcion, latitud, longitud, numero, piso, id_localidad)
VALUES ('Rio Uruguay',NULL,'''',NULL,NULL,15000,NULL,1),
       ('Rio Parana',NULL,'''',NULL,NULL,14500,NULL,1),
       ('Rio Chico',NULL,'''',NULL,NULL,4589,NULL,1),
       ('Av Armenia',NULL,'''',NULL,NULL,45677,NULL,1),
       ('Rio Piacentin',NULL,'',NULL,NULL,345,NULL,1);

INSERT INTO sucursal (categoriaIva, eliminada, email, fecha_inicio_actividad, id_fiscal,
    ingresos_brutos, lema, logo, nombre, telefono, id_ubicacion)
VALUES ('RESPONSABLE_INSCRIPTO',false,'support@globocorporation.com','2012-08-08 00:00:00',
    23154587589,123456789,'testeando',NULL,'Globo Corporation','3794551122',5);

INSERT INTO usuario (apellido, eliminado, email, habilitado, id_sucursal_predeterminada, nombre,
    password, password_recovery_key, password_recovery_key_expiration_date, token, username)
VALUES ('test',false,'test@test.com',true,1,'Sucursal Test','098f6bcd4621d373cade4e832627b4f6','',NULL,'','test'),
    ('Rockefeller',false,'marce.r@gmail.com',false,0,'Marcelo','9cdcde4755ceeb6b5ad173c606e8997a',0,NULL,NULL,'marce');

INSERT INTO rol (id_usuario, nombre)
VALUES (1,'ADMINISTRADOR'),(1,'ENCARGADO'),(1,'COMPRADOR'),(2,'COMPRADOR');

INSERT INTO formadepago(afecta_caja, eliminada, nombre, predeterminado)
VALUES (true,false,'Efectivo',true);

INSERT INTO cliente(bonificacion, categoriaIva, contacto, eliminado, email, fecha_alta, id_fiscal,
    nombre_fantasia, nombre_fiscal, nro_cliente, predeterminado, telefono, id_usuario_credencial, id_sucursal,
    id_ubicacion_envio, id_ubicacion_facturacion, id_usuario_viajante)
VALUES (10.000000000000000,'RESPONSABLE_INSCRIPTO','',false,'','2019-07-18 02:43:38',20362148952,'Peter Parker',
    'Peter Parker','25158',false,'379123452',2,1,1,2,NULL),(0.000000000000000,'RESPONSABLE_INSCRIPTO','El señor Oscuro',
    false,'Cliente@test.com.br','2019-07-18 02:57:38',2355668,'Cliente test','Cliente test','93413',false,'372461245',1,1,NULL,NULL,NULL);

INSERT INTO transportista(eliminado, nombre, telefono, web, id_ubicacion)
VALUES (false,'Correo OCA','3795402356','pedidos@oca.com.ar',3);

INSERT INTO medida(eliminada, nombre)
VALUES (false,'Metro'),(false,'Kilo');

INSERT INTO proveedor(categoriaIva, contacto, eliminado, email, id_fiscal, razon_social, tel_primario, tel_secundario,
    web, id_sucursal, id_ubicacion, nro_proveedor)
VALUES ('RESPONSABLE_INSCRIPTO','Raul Gamez',false,'chamacosrl@gmail.com',23127895679,'Chamaco S.R.L.','3794356778',
    '123456','www.chamacosrl.com.ar',1,4,1);

INSERT INTO rubro(eliminado, nombre)
VALUES (false,'Ferreteria');

INSERT INTO configuraciondelsistema(cantidad_maxima_de_renglones_en_factura, certificado_afip, email_password,
    email_sender_habilitado, email_username,factura_electronica_habilitada, fecha_generacion_tokenWsaa,
    fecha_vencimiento_tokenWsaa, firmante_certificado_afip,
    nro_punto_de_venta_afip, password_certificado_afip, sign_tokenWsaa, tokenWsaa, usar_factura_venta_pre_impresa, id_sucursal)
VALUES (3000,NULL,NULL,false,NULL,false,NULL,NULL,NULL,1,NULL,NULL,NULL,false,1);

INSERT INTO cuentacorriente(eliminada,fecha_apertura, id_sucursal, saldo, fecha_ultimo_movimiento)
VALUES (false,'2019-07-18 02:43:38',1,0.000000000000000,'2019-07-18 02:43:38'),
    (false,'2019-07-18 02:51:59',1,0.000000000000000,'2019-07-18 02:43:38'),
    (false,'2019-07-18 02:57:38',1,0.000000000000000,'2019-07-18 02:43:38');

INSERT INTO cuentacorrientecliente (id_cuenta_corriente, id_Cliente)
VALUES (1,1), (3,2);

INSERT INTO cuentacorrienteproveedor (id_cuenta_corriente, id_Proveedor)
VALUES (2,1);
