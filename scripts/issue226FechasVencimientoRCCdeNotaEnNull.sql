SET SQL_SAFE_UPDATES = 0;
UPDATE rengloncuentacorriente
SET 
    rengloncuentacorriente.fechaVencimiento = null
WHERE 
rengloncuentacorriente.tipo_comprobante = "NOTA_CREDITO_A" OR 
rengloncuentacorriente.tipo_comprobante = "NOTA_CREDITO_B" OR
rengloncuentacorriente.tipo_comprobante = "NOTA_CREDITO_C" OR
rengloncuentacorriente.tipo_comprobante = "NOTA_CREDITO_X" OR
rengloncuentacorriente.tipo_comprobante = "NOTA_CREDITO_Y" OR
rengloncuentacorriente.tipo_comprobante = "NOTA_CREDITO_PRESUPUESTO";
SET SQL_SAFE_UPDATES = 1;