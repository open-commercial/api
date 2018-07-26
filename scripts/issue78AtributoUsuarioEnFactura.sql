ALTER table factura add column id_Usuario bigint(20) not null after id_Transportista;

 SET SQL_SAFE_UPDATES = 0;

 UPDATE 
     factura 
 inner join facturaventa on factura.id_Factura = facturaventa.id_Factura
 SET 
	factura.id_Usuario = facturaventa.id_Usuario;
 
 SET SQL_SAFE_UPDATES = 1;
		

 SET SQL_SAFE_UPDATES = 0;

 UPDATE 
     factura 
 SET 
	factura.id_Usuario = 1
 WHERE
	factura.id_Usuario = 0;
  

 SET SQL_SAFE_UPDATES = 1;

SET SQL_SAFE_UPDATES = 0;
SET foreign_key_checks = 0;
SET UNIQUE_CHECKS = 0; 

 ALTER TABLE facturaventa DROP FOREIGN KEY FKr58rs6i7mo2ow1d09o5yxb7vk;
 ALTER TABLE facturaventa DROP COLUMN id_Usuario;	

SET foreign_key_checks = 1;
SET UNIQUE_CHECKS = 1; 	
SET SQL_SAFE_UPDATES = 1;


 SET SQL_SAFE_UPDATES = 0;
 SET foreign_key_checks = 0;
 SET UNIQUE_CHECKS = 0; 
 
ALTER TABLE factura
ADD CONSTRAINT FKr58rs6i7mo2ow1d09o5yxb7vk
FOREIGN KEY (id_Usuario) REFERENCES usuario(id_Usuario);

 SET foreign_key_checks = 1;
 SET UNIQUE_CHECKS = 1; 	
 SET SQL_SAFE_UPDATES = 1;

