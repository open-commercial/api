SET SQL_SAFE_UPDATES = 0;
UPDATE cuentacorriente AS b1, (SELECT SUM(rengloncuentacorriente.monto) as suma, cuentacorriente.id_cuenta_corriente -- , cuentacorriente.id_cuenta_corriente, cuentacorriente.saldo
 FROM rengloncuentacorriente  inner join cuentacorriente on
rengloncuentacorriente.id_cuenta_corriente = cuentacorriente.id_cuenta_corriente
where rengloncuentacorriente.eliminado = false
group by rengloncuentacorriente.id_cuenta_corriente)
 AS b2
SET b1.saldo = b2.suma
WHERE b1.id_cuenta_corriente = b2.id_cuenta_corriente;
SET SQL_SAFE_UPDATES = 1;