START TRANSACTION;

INSERT INTO sic.rol (id_Usuario, nombre)
(SELECT u.id_Usuario, 'ADMINISTRADOR' 
FROM sic.usuario u 
WHERE u.permisosAdministrador = true);

INSERT INTO sic.rol (id_Usuario, nombre)
(SELECT u.id_Usuario, 'VENDEDOR' 
FROM sic.usuario u 
WHERE u.permisosAdministrador = false);

ALTER TABLE `sic`.`usuario` 
DROP COLUMN `permisosAdministrador`;

COMMIT;
