ALTER TABLE caja DROP COLUMN fechaCorteInforme;
ALTER TABLE caja CHANGE COLUMN saldoFinal saldoSistema decimal(25,15);