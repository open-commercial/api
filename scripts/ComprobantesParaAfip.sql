-- FACTURAS A y B
SELECT factura.fecha, factura.tipoComprobante, factura.numSerieAfip, factura.numFacturaAfip, factura.CAE,
	cliente.idFiscal AS 'CUIT', cliente.razonSocial, cliente.nombreFantasia, cliente.categoriaIVA, localidad.nombre AS 'localidad',
    provincia.nombre AS 'provincia', factura.subTotal_bruto, factura.iva_105_neto, factura.iva_21_neto,
    (100 * factura.iva_105_neto) / 10.5 AS 'base_imponible_105', ((100 * factura.iva_21_neto) / 21) AS 'base_imponible_21', factura.total
FROM factura INNER JOIN facturaventa ON factura.id_Factura = facturaventa.id_Factura
	INNER JOIN cliente ON facturaventa.id_Cliente = cliente.id_Cliente
  INNER JOIN localidad ON cliente.id_Localidad = localidad.id_Localidad
  INNER JOIN provincia ON localidad.id_Provincia = provincia.id_Provincia
WHERE (factura.tipoComprobante = 'FACTURA_A' OR factura.tipoComprobante = 'FACTURA_B')
	AND (factura.fecha >= '2018-09-01 00:00:00' AND factura.fecha <= '2018-09-30 23:59:59')
	AND factura.eliminada = 0 AND factura.id_Empresa = 1
ORDER BY factura.tipoComprobante, factura.fecha ASC
LIMIT 0,1000000;

-- NOTAS CREDITO A y B
SELECT nota.fecha, nota.tipoComprobante, nota.numSerieAfip, nota.numNotaAfip, nota.CAE,
	cliente.idFiscal AS 'CUIT', cliente.razonSocial, cliente.nombreFantasia, cliente.categoriaIVA, localidad.nombre AS 'localidad',
    provincia.nombre AS 'provincia', nota.subTotalBruto, nota.iva105neto, nota.iva21neto,
    (100 * nota.iva105neto) / 10.5 AS 'base_imponible_105', ((100 * nota.iva21neto) / 21) AS 'base_imponible_21', nota.total
FROM nota INNER JOIN notacredito on nota.idNota = notacredito.idNota
	INNER JOIN cliente ON nota.id_Cliente = cliente.id_Cliente
  INNER JOIN localidad ON cliente.id_Localidad = localidad.id_Localidad
  INNER JOIN provincia ON localidad.id_Provincia = provincia.id_Provincia
WHERE (nota.tipoComprobante = 'NOTA_CREDITO_A' OR nota.tipoComprobante = 'NOTA_CREDITO_B')
	AND (nota.fecha >= '2018-09-01 00:00:00' AND nota.fecha <= '2018-09-30 23:59:59')
	AND nota.eliminada = 0 AND nota.id_Empresa = 1
ORDER BY nota.tipoComprobante, nota.fecha ASC
LIMIT 0,1000000;

-- NOTAS DEBITO A y B
SELECT nota.fecha, nota.tipoComprobante, nota.numSerieAfip, nota.numNotaAfip, nota.CAE,
	cliente.idFiscal AS 'CUIT', cliente.razonSocial, cliente.nombreFantasia, cliente.categoriaIVA, localidad.nombre AS 'localidad',
    provincia.nombre AS 'provincia', nota.subTotalBruto, nota.iva105neto, nota.iva21neto,
    (100 * nota.iva105neto) / 10.5 AS 'base_imponible_105', ((100 * nota.iva21neto) / 21) AS 'base_imponible_21', nota.total
FROM nota INNER JOIN notadebito on nota.idNota = notadebito.idNota
	INNER JOIN cliente ON nota.id_Cliente = cliente.id_Cliente
  INNER JOIN localidad ON cliente.id_Localidad = localidad.id_Localidad
  INNER JOIN provincia ON localidad.id_Provincia = provincia.id_Provincia
WHERE (nota.tipoComprobante = 'NOTA_DEBITO_A' OR nota.tipoComprobante = 'NOTA_DEBITO_B')
	AND (nota.fecha >= '2018-09-01 00:00:00' AND nota.fecha <= '2018-09-30 23:59:59')
	AND nota.eliminada = 0 AND nota.id_Empresa = 1
ORDER BY nota.tipoComprobante, nota.fecha ASC
LIMIT 0,1000000;
