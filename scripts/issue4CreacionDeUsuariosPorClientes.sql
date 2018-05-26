-- ALTER TABLE usuario
-- ADD columnaTemporal bigint(20); 
-- -- 

-- INSERT INTO usuario(apellido, eliminado, email,
--   habilitado, nombre, username, password, passwordRecoveryKey, columnaTemporal)
--  SELECT cliente.razonSocial , cliente.eliminado, (SELECT IF(email = "",concat(rand(id_Cliente) * 100000000000000000, "@","globo.com" ), email) AS emailColumn), 
--  false, cliente.razonSocial, rand(id_Cliente) * 100000000000000000, md5("globo123"), 0 , id_Cliente
--  FROM cliente;
-- -- 

-- SET SQL_SAFE_UPDATES=0;
-- update usuario inner join cliente on cliente.id_Cliente = usuario.columnaTemporal
-- set cliente.id_Usuario_Credencial = usuario.id_Usuario
-- where cliente.id_Cliente = usuario.columnaTemporal;
-- SET SQL_SAFE_UPDATES=1;
-- 
-- ALTER TABLE usuario
-- DROP COLUMN columnaTemporal; 
