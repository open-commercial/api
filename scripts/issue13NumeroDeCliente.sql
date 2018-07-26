 ALTER TABLE cliente ADD column nroCliente bigint(20) not null after id_Cliente;

 SET SQL_SAFE_UPDATES = 0;

 UPDATE 
     cliente 
 SET 
	cliente.nroCliente = cliente.id_Cliente;
 
 SET SQL_SAFE_UPDATES = 1;

