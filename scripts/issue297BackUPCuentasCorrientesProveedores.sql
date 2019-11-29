select proveedor.nroProveedor, proveedor.idFiscal as 'CUIT o DNI', proveedor.razonSocial as 'R. Social o Nombre',
     cuentacorriente.saldo, cuentacorriente.fechaUltimoMovimiento,
    proveedor.contacto, proveedor.telPrimario, proveedor.telSecundario, proveedor.email
from cuentacorriente inner join cuentacorrienteproveedor on cuentacorriente.id_cuenta_corriente = cuentacorrienteproveedor.id_cuenta_corriente
	inner join proveedor on proveedor.id_Proveedor = cuentacorrienteproveedor.id_Proveedor
where cuentacorriente.eliminada = false and cuentacorriente.id_Empresa = 5
order by cuentacorriente.saldo