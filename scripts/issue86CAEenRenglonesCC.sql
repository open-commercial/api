ALTER table rengloncuentacorriente add column CAE bigint(20) after id_renglon_cuenta_corriente;

SET SQL_SAFE_UPDATES = 0;

UPDATE 
    rengloncuentacorriente inner join factura on rengloncuentacorriente.id_Factura = factura.id_Factura
SET 
	rengloncuentacorriente.CAE = factura.CAE
WHERE factura.CAE > 0;
UPDATE 
    rengloncuentacorriente inner join nota on rengloncuentacorriente.idNota = nota.idNota
SET 
	rengloncuentacorriente.CAE = nota.CAE
WHERE nota.CAE > 0;

SET SQL_SAFE_UPDATES = 1;
		