-- NOTA CREDITO
USE sic;
SELECT nota.fecha, nota.tipoComprobante, nota.numSerieAfip, nota.numNotaAfip, nota.CAE,
	cliente.idFiscal AS 'CUIT', cliente.razonSocial, cliente.nombreFantasia, condicioniva.nombre AS 'condicion', localidad.nombre AS 'localidad',
    provincia.nombre AS 'provincia', nota.subTotalBruto, nota.iva105neto, nota.iva21neto,
    (100 * nota.iva105neto) / 10.5 AS 'base_imponible_105', ((100 * nota.iva21neto) / 21) AS 'base_imponible_21', nota.total
FROM nota INNER JOIN notacredito on nota.idNota = notacredito.idNota
	INNER JOIN cliente ON nota.id_Cliente = cliente.id_Cliente INNER JOIN condicioniva ON cliente.id_CondicionIVA = condicioniva.id_CondicionIVA
    INNER JOIN localidad ON cliente.id_Localidad = localidad.id_Localidad INNER JOIN provincia ON localidad.id_Provincia = provincia.id_Provincia
WHERE (nota.tipoComprobante = 'NOTA_CREDITO_A' OR nota.tipoComprobante = 'NOTA_CREDITO_B')
	AND (nota.fecha >= '2017-07-01 00:00:00' AND nota.fecha <= '2017-07-31 23:59:59')
	AND nota.eliminada = 0
ORDER BY nota.tipoComprobante, nota.fecha ASC;
-- NOTA DEBITO
USE sic;
SELECT nota.fecha, nota.tipoComprobante, nota.numSerieAfip, nota.numNotaAfip, nota.CAE,
	cliente.idFiscal AS 'CUIT', cliente.razonSocial, cliente.nombreFantasia, condicioniva.nombre AS 'condicion', localidad.nombre AS 'localidad',
    provincia.nombre AS 'provincia', nota.subTotalBruto, nota.iva105neto, nota.iva21neto,
    (100 * nota.iva105neto) / 10.5 AS 'base_imponible_105', ((100 * nota.iva21neto) / 21) AS 'base_imponible_21', nota.total
FROM nota INNER JOIN notadebito on nota.idNota = notadebito.idNota
	INNER JOIN cliente ON nota.id_Cliente = cliente.id_Cliente INNER JOIN condicioniva ON cliente.id_CondicionIVA = condicioniva.id_CondicionIVA
    INNER JOIN localidad ON cliente.id_Localidad = localidad.id_Localidad INNER JOIN provincia ON localidad.id_Provincia = provincia.id_Provincia
WHERE (nota.tipoComprobante = 'NOTA_DEBITO_A' OR nota.tipoComprobante = 'NOTA_DEBITO_B')
	AND (nota.fecha >= '2017-07-01 00:00:00' AND nota.fecha <= '2017-07-31 23:59:59')
	AND nota.eliminada = 0
ORDER BY nota.tipoComprobante, nota.fecha ASC;