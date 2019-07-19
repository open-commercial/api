INSERT INTO provincia VALUES (1,'Corrientes'),(2,'Misiones'),(3,'Chaco');

INSERT INTO localidad
VALUES (1,'N3400',0.000000000000000,false,'Corrientes',1),
       (2,'W3470',0.000000000000000,false,'Mercedes',1),
       (3,'N3300',0.000000000000000,false,'Posadas',2),
       (4,'H3500',0.000000000000000,false,'Resistencia',3);

INSERT INTO ubicacion
VALUES (1,'Rio Uruguay',NULL,'',NULL,NULL,15000,NULL,1),
       (2,'Rio Parana',NULL,'',NULL,NULL,14500,NULL,1),
       (3,'Rio Chico',NULL,'',NULL,NULL,4589,NULL,1),
       (4,'Av Armenia',NULL,'',NULL,NULL,45677,NULL,1),
       (5,'Av Armenia',NULL,'',NULL,NULL,45677,NULL,1);

INSERT INTO empresa
VALUES (1,'RESPONSABLE_INSCRIPTO',false,'support@globocorporation.com','2012-08-08 00:00:00',23154587589,123456789,'testeando',NULL,'Globo Corporation','3794551122',5);

INSERT INTO usuario
VALUES (1,'test',false,'test@test.com',true,1,'Empresa Test','098f6bcd4621d373cade4e832627b4f6','',NULL,'','test'),
(2,'Rockefeller',false,'marce.r@gmail.com',false,0,'Marcelo','9cdcde4755ceeb6b5ad173c606e8997a','0',NULL,NULL,'marce');

INSERT INTO rol
VALUES (1,'ADMINISTRADOR'),(1,'ENCARGADO'),(1,'COMPRADOR'),(2,'COMPRADOR');

INSERT INTO formadepago
VALUES (1,true,false,'Efectivo',true,1);


INSERT INTO cliente
VALUES (1,10.000000000000000,'RESPONSABLE_INSCRIPTO','',false,'','2019-07-18 02:43:38',20362148952,'Peter Parker','Peter Parker','25158',false,'379123452',2,1,1,2,NULL),
(2,0.000000000000000,'RESPONSABLE_INSCRIPTO','El señor Oscuro',false,'Cliente@test.com.br','2019-07-18 02:57:38',2355668,'Cliente test','Cliente test','93413',false,'372461245',1,1,NULL,NULL,NULL);

INSERT INTO transportista
VALUES (1,false,'Correo OCA','3795402356','pedidos@oca.com.ar',1,3);

INSERT INTO medida
VALUES (1,false,'Metro',1),(2,false,'Kilo',1);

INSERT INTO proveedor
VALUES (1,'RESPONSABLE_INSCRIPTO','Raul Gamez',false,'chamacosrl@gmail.com',23127895679,'45539','Chamaco S.R.L.','3794356778','3794894514','www.chamacosrl.com.ar',1,4);

INSERT INTO rubro
VALUES (1,false,'Ferreteria',1);

INSERT INTO configuraciondelsistema
VALUES (1,3000,NULL,NULL,false,NULL,false,NULL,NULL,NULL,1,NULL,NULL,NULL,false,1);

INSERT INTO cuentacorriente
VALUES (1,false,'2019-07-18 02:43:38',NULL,0.000000000000000,1),
(2,false,'2019-07-18 02:51:59',NULL,0.000000000000000,1),
(3,false,'2019-07-18 02:57:38',NULL,0.000000000000000,1);

INSERT INTO cuentacorrientecliente
VALUES (1,1), (3,2);

INSERT INTO cuentacorrienteproveedor
VALUES (2,1);