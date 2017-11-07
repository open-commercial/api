DELIMITER $$
 DROP PROCEDURE IF EXISTS test_mysql_while_loop$$
 CREATE PROCEDURE test_mysql_while_loop()
 BEGIN
 DECLARE x  INT;
 
 SET x = 0 , @idPaisInc = (SELECT max(id_Pais) FROM sic.pais) + 1,
 @contador = (SELECT count(*) FROM tabla.pais);
 
 WHILE x  < @contador DO
   INSERT INTO sic.pais (id_Pais, eliminado, nombre) values ();
--     (
--     SELECT @idPaisInc, eliminado, nombre  FROM tabla.pais
--     );
 SET  x = x + 1; 
 SET  @idPaisInc = @idPaisInc + 1; 
 END WHILE;

 END$$

CALL test_mysql_while_loop();