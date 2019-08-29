START TRANSACTION; 
SET SQL_SAFE_UPDATES=0;

update rengloncuentacorriente as RCC
inner join notacreditocliente as NCC on RCC.idNota = NCC.idNota
inner join nota as N on NCC.idNota = N.idNota
set RCC.CAE = N.CAE
where N.CAE != 0
and RCC.CAE is null;

update rengloncuentacorriente as RCC
inner join notadebitocliente as NDC on RCC.idNota = NDC.idNota
inner join nota as N on NDC.idNota = N.idNota
set RCC.CAE = N.CAE
where N.CAE != 0
and RCC.CAE is null;

SET SQL_SAFE_UPDATES=1;
COMMIT;


