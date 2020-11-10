SET SQL_SAFE_UPDATES = 0;

update rengloncuentacorriente
set tipo_comprobante = 'REMITO'
where tipo_comprobante = 'REMITO_A' or 
tipo_comprobante = 'REMITO_B' or
tipo_comprobante = 'REMITO_C' or
tipo_comprobante = 'REMITO_X' or
tipo_comprobante = 'REMITO_PRESUPUESTO';

SET SQL_SAFE_UPDATES = 1;
