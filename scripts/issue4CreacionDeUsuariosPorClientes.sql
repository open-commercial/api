 ALTER TABLE usuario ADD columnaTemporal bigint(20);

 SET SQL_SAFE_UPDATES = 0;
 SET foreign_key_checks = 0;
 SET UNIQUE_CHECKS = 0;
 INSERT INTO usuario(apellido, eliminado, email, habilitado, nombre, username, password, passwordRecoveryKey, columnaTemporal)
  SELECT "-" , cliente.eliminado,
    (SELECT IF(email = "", concat(round(rand(id_Cliente) * 10000, 0), "@","notiene.com" ), email) AS emailColumn),
	  false, cliente.razonSocial, round(rand(id_Cliente) * 10000,0) , md5("12345"), 0 , id_Cliente
  FROM cliente
  WHERE cliente.id_Empresa = 1  AND cliente.eliminado = false;
 SET SQL_SAFE_UPDATES = 1;
 SET foreign_key_checks = 1;
 SET UNIQUE_CHECKS = 1;

 SET SQL_SAFE_UPDATES = 0;
 SET foreign_key_checks = 0;
 SET UNIQUE_CHECKS = 0;
 INSERT INTO rol(id_Usuario, nombre)
 SELECT usuario.id_Usuario, "COMPRADOR"
 FROM usuario
 WHERE usuario.id_Usuario > 34; -- id ultimo usuario
 SET SQL_SAFE_UPDATES = 1;
 SET foreign_key_checks = 1;
 SET UNIQUE_CHECKS = 1;

 SET SQL_SAFE_UPDATES=0;
 update usuario inner join cliente on cliente.id_Cliente = usuario.columnaTemporal
 set cliente.id_Usuario_Credencial = usuario.id_Usuario
 where cliente.id_Cliente = usuario.columnaTemporal;
 SET SQL_SAFE_UPDATES=1;

 ALTER TABLE usuario DROP COLUMN usuario.columnaTemporal;