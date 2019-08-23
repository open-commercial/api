START TRANSACTION; 
SET SQL_SAFE_UPDATES=0;

update rengloncuentacorriente as RCC
inner join notacreditocliente as NCC on RCC.idNota = NCC.idNota
inner join nota as N on NCC.idNota = N.idNota
set RCC.cae = N.cae
where N.cae != 0
and RCC.cae is null;

update rengloncuentacorriente as RCC
inner join notadebitocliente as NDC on RCC.idNota = NDC.idNota
inner join nota as N on NDC.idNota = N.idNota
set RCC.cae = N.cae
where N.cae != 0
and RCC.cae is null;

SET SQL_SAFE_UPDATES=1;
COMMIT;


