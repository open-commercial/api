alter TABLE formadepago DROP FOREIGN KEY  FK6v6reo24igck98mwjs9b18c2j;
alter TABLE formadepago drop column id_Empresa;

INSERT INTO formadepago (afectaCaja, eliminada, nombre, predeterminado)
   VALUES
    (false, false, "Cheques Propios", false);

SET SQL_SAFE_UPDATES = 0;
 SET FOREIGN_KEY_CHECKS = 0;

UPDATE gasto SET gasto.concepto = CONCAT(gasto.concepto," Forma de pago anterior: Cheque Globo Distribuciones")
where id_FormaDePago = 19;
UPDATE gasto SET gasto.concepto = CONCAT(gasto.concepto, " Forma de pago anterior: Cheque Calvano Mario R")
where id_FormaDePago = 20;
UPDATE gasto SET gasto.concepto = CONCAT(gasto.concepto, " Forma de pago anterior: Cheque para Pagos")
where id_FormaDePago = 51;
UPDATE gasto SET gasto.concepto = CONCAT(gasto.concepto, " Forma de pago anterior: Cheque")
where  id_FormaDePago = 52;

UPDATE gasto SET id_FormaDePago = 39 where id_FormaDePago = 53;
UPDATE gasto SET id_FormaDePago = 16 where id_FormaDePago = 37;
UPDATE gasto SET id_FormaDePago = 15 where id_FormaDePago = 55;
UPDATE gasto SET id_FormaDePago = 61 
where id_FormaDePago = 19 or id_FormaDePago = 20 or id_FormaDePago = 51 or id_FormaDePago = 52;

UPDATE recibo SET recibo.concepto = CONCAT("(Cheque Globo Distribuciones) ", recibo.concepto)
where id_FormaDePago = 19;
UPDATE recibo SET recibo.concepto = CONCAT("(Cheque Calvano Mario R) ", recibo.concepto)
where id_FormaDePago = 20;
UPDATE recibo SET recibo.concepto = CONCAT("(Cheque para Pagos) ", recibo.concepto)
where id_FormaDePago = 51;
UPDATE recibo SET recibo.concepto = CONCAT("(Cheque) ", recibo.concepto)
where  id_FormaDePago = 52;

UPDATE recibo SET id_FormaDePago = 39 where id_FormaDePago = 53;
UPDATE recibo SET id_FormaDePago = 16 where id_FormaDePago = 37;
UPDATE recibo SET id_FormaDePago = 15 where id_FormaDePago = 55;
UPDATE recibo SET id_FormaDePago = 61 
where id_FormaDePago = 19 or id_FormaDePago = 20 or id_FormaDePago = 51 or id_FormaDePago = 52;

DELETE FROM formadepago WHERE id_FormaDePago = 53 
or id_FormaDePago = 37 or id_FormaDePago = 55 or id_FormaDePago = 19 or id_FormaDePago = 20 
or id_FormaDePago = 51 or id_FormaDePago = 52;

UPDATE formadepago SET nombre='Master Debito' WHERE id_FormaDePago=57;
UPDATE formadepago SET eliminada=true WHERE id_FormaDePago= 10;
UPDATE formadepago SET eliminada=true WHERE id_FormaDePago=56;

ALTER TABLE `recibo` 
ADD COLUMN `idPagoMercadoPago` varchar(255) AFTER `numRecibo`;


SET SQL_SAFE_UPDATES = 1;
SET FOREIGN_KEY_CHECKS = 1;