ALTER table rengloncuentacorriente add column cae bigint(20) after id_renglon_cuenta_corriente;

SET SQL_SAFE_UPDATES = 0;

UPDATE 
    rengloncuentacorriente inner join factura on rengloncuentacorriente.id_Factura = factura.id_Factura
SET 
	rengloncuentacorriente.cae = factura.cae
WHERE factura.cae > 0;
UPDATE 
    rengloncuentacorriente inner join nota on rengloncuentacorriente.idNota = nota.idNota
SET 
	rengloncuentacorriente.cae = nota.cae
WHERE nota.cae > 0;

SET SQL_SAFE_UPDATES = 1;
		