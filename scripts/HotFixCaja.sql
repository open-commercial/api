ALTER TABLE caja CHANGE COLUMN saldoInicial saldoApertura decimal(25,15) NOT NULL;
ALTER TABLE caja DROP COLUMN observacion;
ALTER TABLE caja DROP nroCaja;