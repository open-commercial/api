START TRANSACTION;
SET SQL_SAFE_UPDATES = 0;
set @i = (SELECT max(sic.pago.nroPago) FROM sic.pago);
update sic.pago set sic.pago.nroPago=(@i:=@i+1) where sic.pago.nroPago = 0;
COMMIT;
