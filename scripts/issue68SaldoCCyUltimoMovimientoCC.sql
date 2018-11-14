alter table cuentacorriente add column saldo decimal(25,15),
add column fechaUltimoMovimiento datetime;

SET SQL_SAFE_UPDATES = 0;

update
cuentacorriente inner join 
(SELECT cuentacorriente.id_cuenta_corriente, SUM(rengloncuentacorriente.monto) as saldoCC FROM cuentacorriente INNER JOIN rengloncuentacorriente 
on cuentacorriente.id_cuenta_corriente = rengloncuentacorriente.id_cuenta_corriente
 WHERE cuentacorriente.eliminada = false AND rengloncuentacorriente.eliminado = false
 group by cuentacorriente.id_cuenta_corriente) as t2 on cuentacorriente.id_cuenta_corriente = t2.id_cuenta_corriente
set
cuentacorriente.saldo = t2.saldoCC; 

update
cuentacorriente inner join 
(SELECT cuentacorriente.id_cuenta_corriente, max(rengloncuentacorriente.fecha) as fechaUltimoMovimiento FROM cuentacorriente INNER JOIN rengloncuentacorriente 
on cuentacorriente.id_cuenta_corriente = rengloncuentacorriente.id_cuenta_corriente
WHERE cuentacorriente.eliminada = false AND rengloncuentacorriente.eliminado = false
group by cuentacorriente.id_cuenta_corriente) as t2 on cuentacorriente.id_cuenta_corriente = t2.id_cuenta_corriente
set
cuentacorriente.fechaUltimoMovimiento = t2.fechaUltimoMovimiento; 

update
cuentacorriente 
set 
cuentacorriente.saldo = 0
where cuentacorriente.saldo is null; 

SET SQL_SAFE_UPDATES=1;