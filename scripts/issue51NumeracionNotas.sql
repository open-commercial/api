START TRANSACTION;
SET SQL_SAFE_UPDATES = 0;
set @i = 0;
update nota set nota.nroNota=(@i:=@i+1) WHERE nota.tipoComprobante = "NOTA_CREDITO_A";
COMMIT;

START TRANSACTION;
SET SQL_SAFE_UPDATES = 0;
set @i = 0;
update nota set nota.nroNota=(@i:=@i+1) WHERE nota.tipoComprobante = "NOTA_CREDITO_B";
COMMIT;

START TRANSACTION;
SET SQL_SAFE_UPDATES = 0;
set @i = 0;
update nota set nota.nroNota=(@i:=@i+1) WHERE nota.tipoComprobante = "NOTA_CREDITO_X";
COMMIT;

START TRANSACTION;
SET SQL_SAFE_UPDATES = 0;
set @i = 0;
update nota set nota.nroNota=(@i:=@i+1) WHERE nota.tipoComprobante = "NOTA_CREDITO_Y";
COMMIT;

START TRANSACTION;
SET SQL_SAFE_UPDATES = 0;
set @i = 0;
update nota set nota.nroNota=(@i:=@i+1) WHERE nota.tipoComprobante = "NOTA_CREDITO_PRESUPUESTO";
COMMIT;

START TRANSACTION;
SET SQL_SAFE_UPDATES = 0;
set @i = 0;
update nota set nota.nroNota=(@i:=@i+1) WHERE nota.tipoComprobante = "NOTA_DEBITO_A";
COMMIT;

START TRANSACTION;
SET SQL_SAFE_UPDATES = 0;
set @i = 0;
update nota set nota.nroNota=(@i:=@i+1) WHERE nota.tipoComprobante = "NOTA_DEBITO_B";
COMMIT;

START TRANSACTION;
SET SQL_SAFE_UPDATES = 0;
set @i = 0;
update nota set nota.nroNota=(@i:=@i+1) WHERE nota.tipoComprobante = "NOTA_DEBITO_X";
COMMIT;

START TRANSACTION;
SET SQL_SAFE_UPDATES = 0;
set @i = 0;
update nota set nota.nroNota=(@i:=@i+1) WHERE nota.tipoComprobante = "NOTA_DEBITO_Y";
COMMIT;

START TRANSACTION;
SET SQL_SAFE_UPDATES = 0;
set @i = 0;
update nota set nota.nroNota=(@i:=@i+1) WHERE nota.tipoComprobante = "NOTA_DEBITO_PRESUPUESTO";
COMMIT;
