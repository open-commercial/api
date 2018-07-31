 ALTER TABLE cliente ADD column nroCliente bigint(20) not null after id_Cliente;

 SET SQL_SAFE_UPDATES = 0;

 UPDATE 
     cliente 
 SET 
	cliente.nroCliente = FLOOR(rand(cliente.id_Cliente) * (99999 - 10000 + 1)) + 10000;
 
 SET SQL_SAFE_UPDATES = 1;

