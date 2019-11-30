select cliente.nroCliente, cliente.idFiscal as 'CUIT o DNI', cliente.nombreFiscal as 'R. Social o Nombre',
    cliente.nombreFantasia, cuentacorriente.saldo, cuentacorriente.fechaUltimoMovimiento,
    concat(usuario.nombre, ' ', usuario.apellido) as 'Viajante',
    cliente.contacto, cliente.telefono, cliente.email
from cuentacorriente inner join cuentacorrientecliente on cuentacorriente.id_cuenta_corriente = cuentacorrientecliente.id_cuenta_corriente
	inner join cliente on cliente.id_Cliente = cuentacorrientecliente.id_Cliente
    left join usuario on usuario.id_Usuario = cliente.id_Usuario_Viajante
where cuentacorriente.eliminada = false and cuentacorriente.id_Empresa = 1
order by cuentacorriente.saldo