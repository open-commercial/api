ALTER TABLE empresa ADD categoriaIVA varchar(255) NOT NULL AFTER id_Empresa;
ALTER TABLE cliente ADD categoriaIVA varchar(255) NOT NULL AFTER nroCliente;
ALTER TABLE proveedor ADD categoriaIVA varchar(255) NOT NULL AFTER codigo;

UPDATE empresa SET categoriaIVA = CASE
    WHEN empresa.id_CondicionIVA = 1 THEN "RESPONSABLE_INSCRIPTO"
    WHEN empresa.id_CondicionIVA = 12 THEN "EXENTO"
    WHEN empresa.id_CondicionIVA = 10 THEN "CONSUMIDOR_FINAL"
    WHEN empresa.id_CondicionIVA = 11 THEN "MONOTRIBUTO"
    ELSE "CONSUMIDOR_FINAL"
    END
WHERE empresa.id_CondicionIVA in (1,12,10,11);

ALTER TABLE empresa ADD idFiscal BIGINT(20) NULL DEFAULT NULL AFTER id_Empresa;
ALTER TABLE empresa CHANGE COLUMN ingresosBrutos ingresosBrutos BIGINT(20) NULL;

SET SQL_SAFE_UPDATES = 0;
UPDATE empresa SET idFiscal = cuip; 
SET SQL_SAFE_UPDATES = 1;

ALTER TABLE empresa DROP COLUMN cuip;

UPDATE cliente SET categoriaIVA = CASE
    WHEN cliente.id_CondicionIVA = 1 THEN "RESPONSABLE_INSCRIPTO"
    WHEN cliente.id_CondicionIVA = 12 THEN "EXENTO"
    WHEN cliente.id_CondicionIVA = 10 THEN "CONSUMIDOR_FINAL"
    WHEN cliente.id_CondicionIVA = 11 THEN "MONOTRIBUTO"
    ELSE "CONSUMIDOR_FINAL"
    END
WHERE cliente.id_CondicionIVA in (1,12,10,11);

UPDATE proveedor SET categoriaIVA = CASE
    WHEN proveedor.id_CondicionIVA = 1 THEN "RESPONSABLE_INSCRIPTO"
    WHEN proveedor.id_CondicionIVA = 12 THEN "EXENTO"
    WHEN proveedor.id_CondicionIVA = 10 THEN "CONSUMIDOR_FINAL"
    WHEN proveedor.id_CondicionIVA = 11 THEN "MONOTRIBUTO"
    ELSE "CONSUMIDOR_FINAL"
    END
WHERE proveedor.id_CondicionIVA in (1,12,10,11);

ALTER TABLE empresa
  DROP foreign key FKoe8ihwidpastxfeneq5k4vs07,
  DROP COLUMN empresa.id_CondicionIVA;
  
ALTER TABLE cliente
  DROP foreign key FKm5l8c91knfxk0w27btt6x3vro,
  DROP COLUMN cliente.id_CondicionIVA;

ALTER TABLE proveedor
  DROP foreign key FK4hiu7610oh99ykb29eale9pg9,
  DROP COLUMN proveedor.id_CondicionIVA;

DROP TABLE condicioniva;

-----------------------------------------

-- PROVEEDORES
SET SQL_SAFE_UPDATES = 0;
UPDATE proveedor SET idFiscal = replace(idFiscal, '-', '');
SET SQL_SAFE_UPDATES = 1;

SET SQL_SAFE_UPDATES = 0;
update proveedor set idFiscal = null 
where idFiscal = '';
SET SQL_SAFE_UPDATES = 1;

-- QUITAR NotNull
ALTER TABLE proveedor CHANGE COLUMN idFiscal idFiscal VARCHAR(255) CHARACTER SET 'utf8' NULL;
-- CAMBIAR DE TIPO
ALTER TABLE proveedor CHANGE COLUMN idFiscal idFiscal BIGINT(20) NULL DEFAULT NULL;


-- CLIENTES
ALTER TABLE cliente ADD COLUMN tipoDeCliente VARCHAR(255) NOT NULL AFTER nroCliente;

SET SQL_SAFE_UPDATES = 0;
UPDATE cliente SET tipoDeCliente = CASE
    WHEN cliente.categoriaIVA = "RESPONSABLE_INSCRIPTO" THEN "EMPRESA"
    WHEN cliente.categoriaIVA = "EXENTO" THEN "EMPRESA"
    WHEN cliente.categoriaIVA = "CONSUMIDOR_FINAL" THEN "PERSONA"
    WHEN cliente.categoriaIVA = "MONOTRIBUTO" THEN "PERSONA"        
    ELSE "CONSUMIDOR_FINAL"
    END
WHERE cliente.categoriaIVA in ("RESPONSABLE_INSCRIPTO","EXENTO","CONSUMIDOR_FINAL","MONOTRIBUTO");
SET SQL_SAFE_UPDATES = 1;

SET SQL_SAFE_UPDATES = 0;
UPDATE cliente SET idFiscal = replace(idFiscal, '-', '');
SET SQL_SAFE_UPDATES = 1;

SET SQL_SAFE_UPDATES = 0;
update cliente set idFiscal = null 
where idFiscal = '';
SET SQL_SAFE_UPDATES = 1;

SET SQL_SAFE_UPDATES = 0;
UPDATE cliente SET idFiscal = replace(idFiscal, ' ', '');
SET SQL_SAFE_UPDATES = 1;

-- CAMBIAR DE TIPO
ALTER TABLE cliente CHANGE COLUMN idFiscal idFiscal BIGINT(20) NULL DEFAULT NULL;




