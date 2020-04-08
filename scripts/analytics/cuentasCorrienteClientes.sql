
-- LISTADO DE CUENTAS CORRIENTE CLIENTE CON INFO COMPLETA DE CLIENTE

select cliente.nroCliente, cliente.idFiscal as 'CUIT o DNI', cliente.nombreFiscal as 'R. Social o Nombre',
    cliente.nombreFantasia, cuentacorriente.saldo, cuentacorriente.fechaUltimoMovimiento,
    cliente.montoCompraMinima,
    concat(usuario.nombre, ' ', usuario.apellido) as 'Viajante',
    cliente.contacto, cliente.telefono, cliente.email,
    ubicacion.calle, ubicacion.numero, ubicacion.piso, ubicacion.departamento,
    localidad.nombre as 'localidad', provincia.nombre as 'provincia'
from cuentacorriente
    inner join cuentacorrientecliente on cuentacorriente.id_cuenta_corriente = cuentacorrientecliente.id_cuenta_corriente
	inner join cliente on cliente.id_Cliente = cuentacorrientecliente.id_Cliente
    left join ubicacion on cliente.idUbicacionFacturacion = ubicacion.idUbicacion
    left join localidad on localidad.idLocalidad = ubicacion.idLocalidad
    left join provincia on provincia.idProvincia = localidad.idProvincia
    left join usuario on usuario.id_Usuario = cliente.id_Usuario_Viajante
where cuentacorriente.eliminada = false
order by cuentacorriente.saldo
