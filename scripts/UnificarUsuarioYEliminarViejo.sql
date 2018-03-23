SET @idUsuarioViejo = 13;
SET @idUsuarioNuevo = 4;
  
UPDATE caja
  SET caja.id_Usuario =  @idUsuarioNuevo
  WHERE caja.id_Usuario = @idUsuarioViejo;
  
UPDATE cliente
  SET cliente.id_Usuario_Credencial =  @idUsuarioNuevo
  WHERE cliente.id_Usuario_Viajante = @idUsuarioViejo;
  
UPDATE facturaventa
  SET facturaventa.id_Usuario =  @idUsuarioNuevo
  WHERE facturaventa.id_Usuario = @idUsuarioViejo;
  
UPDATE gasto
  SET gasto.id_Usuario =  @idUsuarioNuevo
  WHERE gasto.id_Usuario = @idUsuarioViejo;
  
UPDATE itemcarritocompra
  SET itemcarritocompra.id_Usuario =  @idUsuarioNuevo
  WHERE itemcarritocompra.id_Usuario = @idUsuarioViejo;
  
UPDATE nota
  SET nota.id_Usuario =  @idUsuarioNuevo
  WHERE nota.id_Usuario = @idUsuarioViejo;
  
UPDATE pedido
  SET pedido.id_Usuario =  @idUsuarioNuevo
  WHERE pedido.id_Usuario = @idUsuarioViejo;

UPDATE usuario
  SET usuario.eliminado = true
WHERE usuario.id_Usuario = @idUsuarioViejo;