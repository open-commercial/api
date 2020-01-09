
-- SOLO TIENE EN CUENTA LOS COMPROBANTES BLANCOS
-- SETEAR LA FECHA DESEADA EN EL WHERE

select cliente.idFiscal, cliente.nombreFiscal, sum(rengloncuentacorriente.monto) as 'montoAcumulado'
from cuentacorriente inner join cuentacorrientecliente on cuentacorriente.id_cuenta_corriente = cuentacorrientecliente.id_cuenta_corriente
	inner join rengloncuentacorriente on cuentacorriente.id_cuenta_corriente = rengloncuentacorriente.id_cuenta_corriente
	inner join cliente on cuentacorrientecliente.id_Cliente = cliente.id_Cliente
where cuentacorriente.id_Empresa = 1 and rengloncuentacorriente.eliminado = false
	and rengloncuentacorriente.fecha <= '2018-12-31 23:59:59'
    and (rengloncuentacorriente.tipo_comprobante = 'FACTURA_A'
		or rengloncuentacorriente.tipo_comprobante = 'FACTURA_B'
		or rengloncuentacorriente.tipo_comprobante = 'FACTURA_C'
        or rengloncuentacorriente.tipo_comprobante = 'NOTA_CREDITO_A'
        or rengloncuentacorriente.tipo_comprobante = 'NOTA_CREDITO_B'
        or rengloncuentacorriente.tipo_comprobante = 'NOTA_DEBITO_A'
        or rengloncuentacorriente.tipo_comprobante = 'NOTA_DEBITO_B'
        or rengloncuentacorriente.tipo_comprobante = 'RECIBO')
group by cuentacorriente.id_cuenta_corriente
order by montoAcumulado asc
