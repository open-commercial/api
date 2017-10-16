-- recalculo de iva en los renglones de las facturas B y C
START TRANSACTION;
use sic;
SET SQL_SAFE_UPDATES=0;
update factura inner join renglonfactura on factura.id_Factura = renglonfactura.id_Factura
set
renglonfactura.iva_neto = (((100 * renglonfactura.precioUnitario) / (100 + (renglonfactura.iva_porcentaje))) 
* (renglonfactura.iva_porcentaje / 100)) * (1 - (renglonfactura.descuento_porcentaje / 100))
where factura.tipoComprobante = "FACTURA_B"; 
SET SQL_SAFE_UPDATES=0;
COMMIT;

-- Crea la columna nueva subTotal_bruto
START TRANSACTION;
ALTER TABLE sic.factura
ADD COLUMN subTotal_bruto double NOT NULL
AFTER subTotal;
COMMIT;

-- copia los valores de subTotal_neto a subTotal_bruto y setea en cero el subTotal_Neto (FACTURA A)
START TRANSACTION;
use sic;
SET SQL_SAFE_UPDATES=0;
update sic.factura 
SET factura.subTotal_bruto = factura.subTotal_neto 
where factura.tipoComprobante = "FACTURA_A" 
or factura.tipoComprobante = "FACTURA_X"
or factura.tipoComprobante = "FACTURA_Y"
or factura.tipoComprobante = "FACTURA_C"; 
SET SQL_SAFE_UPDATES=1;
COMMIT;
-- 
-- Elimina la columna de sub total neto
START TRANSACTION; 
use sic;
SET SQL_SAFE_UPDATES=0;
ALTER TABLE factura drop column subTotal_neto; 
SET SQL_SAFE_UPDATES=1;
COMMIT;

-- -- Calculo 2, iva 21 en las facturas B y C
START TRANSACTION; 
use sic;
SET SQL_SAFE_UPDATES=0;
update factura as C
inner join (select factura.id_Factura, sum(renglonfactura.cantidad * renglonfactura.iva_neto) as acum 
from factura inner join renglonfactura on factura.id_Factura = renglonfactura.id_Factura
where renglonfactura.iva_porcentaje = 21 
and (factura.tipoComprobante = "FACTURA_B" or factura.tipoComprobante = "FACTURA_C")
group by factura.id_Factura
) as A on C.id_Factura = A.id_Factura
set C.iva_21_neto = A.acum;
SET SQL_SAFE_UPDATES=1;
COMMIT;
-- 
-- Calculo 2, iva 10.5 en las facturas B y C
START TRANSACTION; 
use sic;
SET SQL_SAFE_UPDATES=0;
update factura as C
 inner join (select factura.id_Factura, sum(renglonfactura.cantidad * renglonfactura.iva_neto) as acum 
from factura inner join renglonfactura on factura.id_Factura = renglonfactura.id_Factura
where renglonfactura.iva_porcentaje = 10.5 
and (factura.tipoComprobante = "FACTURA_B" or factura.tipoComprobante = "FACTURA_C")
group by factura.id_Factura
) as A on C.id_Factura = A.id_Factura
set C.iva_105_neto = A.acum;
SET SQL_SAFE_UPDATES=1;
COMMIT;
-- 
-- Calculo subTotal_bruto de B y C
START TRANSACTION; 
use sic;
SET SQL_SAFE_UPDATES=0;
update sic.factura SET factura.subTotal_bruto = factura.total - (factura.iva_105_neto + factura.iva_21_neto)
where (factura.tipoComprobante = "FACTURA_B" or factura.tipoComprobante = "FACTURA_C");
SET SQL_SAFE_UPDATES=1;
COMMIT;


