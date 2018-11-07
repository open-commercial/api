SET SQL_SAFE_UPDATES = 0;
SET foreign_key_checks = 0;
SET UNIQUE_CHECKS = 0; 

SET @idCajaInc = (SELECT max(id_Caja) FROM sic.caja WHERE id_Caja < 10000000);
SET @idClienteInc = (SELECT max(id_Cliente) FROM sic.cliente WHERE id_Cliente < 10000000);
SET @idCondicionIvaInc = (SELECT max(id_CondicionIVA) FROM sic.condicioniva WHERE id_CondicionIVA < 10000000);
SET @idConfiguracionDelSistemaInc = (SELECT max(id_ConfiguracionDelSistema) FROM sic.configuraciondelsistema WHERE id_ConfiguracionDelSistema < 10000000);
SET @idCuentaCorrienteInc = (SELECT max(idCuentaCorriente) FROM sic.cuentacorriente WHERE idCuentaCorriente < 10000000);
SET @idEmpresaInc = (SELECT max(id_Empresa) FROM sic.empresa WHERE id_Empresa < 10000000);
SET @idFacturaInc = (SELECT max(id_Factura) FROM sic.factura WHERE id_Factura < 10000000);
SET @idFormaDePagoInc = (SELECT max(id_FormaDePago) FROM sic.formadepago WHERE id_FormaDePago < 10000000);
SET @idGastoInc = (SELECT max(id_Gasto) FROM sic.gasto WHERE id_Gasto < 10000000);
SET @idLocalidadInc = (SELECT max(id_Localidad) FROM sic.localidad WHERE id_Localidad < 10000000);
SET @idMedidaInc = (SELECT max(id_Medida) FROM sic.medida WHERE id_Medida < 10000000);
SET @idNotaInc = (SELECT max(idNota) FROM sic.nota WHERE idNota < 10000000);
SET @idPagoInc = (SELECT max(id_Pago) FROM sic.pago WHERE id_Pago < 10000000);
SET @idPaisInc = (SELECT max(id_Pais) FROM sic.pais WHERE id_Pais < 10000000);
SET @idPedidoInc = (SELECT max(id_Pedido) FROM sic.pedido WHERE id_Pedido < 10000000);
SET @idProductoInc = (SELECT max(idProducto) FROM sic.producto WHERE idProducto < 10000000);
SET @idProveedorInc = (SELECT max(id_Proveedor) FROM sic.proveedor WHERE id_Proveedor < 10000000);
SET @idPedidoInc = (SELECT max(id_Pedido) FROM sic.pedido WHERE id_Pedido < 10000000);
SET @idProductoInc = (SELECT max(idProducto) FROM sic.producto WHERE idProducto < 10000000);
SET @idProveedorInc = (SELECT max(id_Proveedor) FROM sic.proveedor WHERE id_Proveedor < 10000000);
SET @idProvinciaInc = (SELECT max(id_Provincia) FROM sic.provincia WHERE id_Provincia < 10000000);
SET @idRenglonCuentaCorrienteInc = (SELECT max(idRenglonCuentaCorriente) FROM sic.rengloncuentacorriente WHERE idRenglonCuentaCorriente < 10000000);
SET @idRenglonFacturaInc = (SELECT max(id_RenglonFactura) FROM sic.renglonfactura WHERE id_RenglonFactura < 10000000);
SET @idRenglonNotaCreditoInc = (SELECT max(idRenglonNotaCredito) FROM sic.renglonnotacredito WHERE idRenglonNotaCredito < 10000000);
SET @idRenglonNotaDebitoInc = (SELECT max(idRenglonNotaDebito) FROM sic.renglonnotadebito WHERE idRenglonNotaDebito < 10000000);
SET @idRenglonPedidoInc = (SELECT max(id_RenglonPedido) FROM sic.renglonpedido WHERE id_RenglonPedido < 10000000);
SET @idRolInc = (SELECT max(id_Usuario) FROM sic.rol WHERE id_Usuario < 10000000);
SET @idRubroInc = (SELECT max(id_Rubro) FROM sic.rubro WHERE id_Rubro < 10000000);
SET @idTransportistaInc = (SELECT max(id_Transportista) FROM sic.transportista WHERE id_Transportista < 10000000);
SET @idUsuarioInc = (SELECT max(id_Usuario) FROM sic.usuario WHERE id_Usuario < 10000000);
-- SET @idMovimientoInc = (SELECT max(idMovimiento) FROM sic.rengloncuentacorriente WHERE  idMovimiento < 10000000);
SET @idProductoItemInc = (SELECT max(idProductoItem) FROM sic.renglonnotacredito WHERE  idProductoItem < 10000000);
-- Empresa
UPDATE   empresa
  SET empresa.id_Empresa = empresa.id_Empresa - 10000000 + @idEmpresaInc
  WHERE empresa.id_Empresa > 10000000;
UPDATE   empresa
  SET empresa.id_CondicionIVA = empresa.id_CondicionIVA - 10000000 + @idCondicionIVAInc
  WHERE empresa.id_CondicionIVA > 10000000;
UPDATE   empresa
  SET empresa.id_Localidad = empresa.id_Localidad - 10000000 + @idLocalidadInc
   WHERE empresa.id_Localidad > 10000000;
-- Caja  
UPDATE   caja
  SET caja.id_Caja = caja.id_Caja - 10000000 + @idCajaInc
  WHERE caja.id_Caja > 10000000;
UPDATE   caja
  SET caja.id_Empresa = caja.id_Empresa - 10000000 + @idEmpresaInc
  WHERE caja.id_Empresa > 10000000;
UPDATE   caja
  SET caja.id_Usuario = caja.id_Usuario - 10000000 + @idUsuarioInc
  WHERE caja.id_Usuario > 10000000;
UPDATE   caja
  SET caja.id_UsuarioCierra = caja.id_UsuarioCierra - 10000000 + @idUsuarioInc
  WHERE caja.id_UsuarioCierra > 10000000;
-- Cliente  
UPDATE   cliente
  SET cliente.id_Cliente = cliente.id_Cliente - 10000000 + @idClienteInc
  WHERE cliente.id_Cliente > 10000000;
UPDATE   cliente
  SET cliente.id_CondicionIVA = cliente.id_CondicionIVA - 10000000 + @idCondicionIVAInc
  WHERE cliente.id_CondicionIVA > 10000000;
UPDATE   cliente
  SET cliente.id_Empresa = cliente.id_Empresa - 10000000 + @idEmpresaInc
  WHERE cliente.id_Empresa > 10000000;
UPDATE   cliente
  SET cliente.id_Localidad = cliente.id_Localidad - 10000000 + @idLocalidadInc
  WHERE cliente.id_Localidad > 10000000;
-- viajante y eso?
-- CondicionIVA  
UPDATE   condicioniva
  SET condicioniva.id_CondicionIVA = condicioniva.id_CondicionIVA - 10000000 + @idCondicionIVAInc
  WHERE condicioniva.id_CondicionIVA > 10000000;
-- Cuenta Corriente
UPDATE   cuentacorriente
  SET cuentacorriente.idCuentaCorriente = cuentacorriente.idCuentaCorriente - 10000000 + @idCuentaCorrienteInc
  WHERE cuentacorriente.idCuentaCorriente > 10000000;
UPDATE   cuentacorriente
  SET cuentacorriente.id_Cliente = cuentacorriente.id_Cliente - 10000000 + @idClienteInc
  WHERE cuentacorriente.id_Cliente > 10000000;
UPDATE   cuentacorriente
  SET cuentacorriente.id_Empresa = cuentacorriente.id_Empresa - 10000000 + @idEmpresaInc
  WHERE cuentacorriente.id_Empresa > 10000000;
-- CONFIGURACION DEL SISTEMA
UPDATE   configuraciondelsistema
  SET configuraciondelsistema.id_ConfiguracionDelSistema = configuraciondelsistema.id_ConfiguracionDelSistema - 10000000 + @idConfiguracionDelSistemaInc
  WHERE configuraciondelsistema.id_ConfiguracionDelSistema > 10000000;  
UPDATE   configuraciondelsistema
  SET configuraciondelsistema.id_Empresa = configuraciondelsistema.id_Empresa - 10000000 + @idEmpresaInc
  WHERE configuraciondelsistema.id_Empresa > 10000000; 
-- FACTURA
UPDATE   factura
  SET factura.id_Factura = factura.id_Factura - 10000000 + @idFacturaInc
  WHERE factura.id_Factura > 10000000;
UPDATE   factura
  SET factura.id_Pedido = factura.id_Pedido - 10000000 + @idPedidoInc
  WHERE factura.id_Pedido > 10000000;
UPDATE   factura
  SET factura.id_Empresa = factura.id_Empresa - 10000000 + @idEmpresaInc
  WHERE factura.id_Empresa > 10000000;
UPDATE   factura
  SET factura.id_Transportista = factura.id_Transportista - 10000000 + @idTransportistaInc
  WHERE factura.id_Transportista > 10000000;
-- Factura Compra
UPDATE   facturacompra
  SET facturacompra.id_Factura = facturacompra.id_Factura - 10000000 + @idFacturaInc
  WHERE facturacompra.id_Factura > 10000000;
UPDATE   facturacompra
  SET facturacompra.id_Proveedor = facturacompra.id_Proveedor - 10000000 + @idProveedorInc
  WHERE facturacompra.id_Proveedor > 10000000;
-- Factura Venta
UPDATE   facturaventa
  SET facturaventa.id_Cliente = facturaventa.id_Cliente - 10000000 + @idClienteInc
  WHERE facturaventa.id_Cliente > 10000000;
UPDATE   facturaventa
  SET facturaventa.id_Factura = facturaventa.id_Factura - 10000000 + @idFacturaInc
  WHERE facturaventa.id_Factura > 10000000;
UPDATE   facturaventa
  SET facturaventa.id_Usuario = facturaventa.id_Usuario - 10000000 + @idUsuarioInc
  WHERE facturaventa.id_Usuario > 10000000;
-- Forma de Pago
UPDATE   formadepago
  SET formadepago.id_Empresa = formadepago.id_Empresa - 10000000 + @idEmpresaInc
  WHERE formadepago.id_Empresa > 10000000;
UPDATE   formadepago
  SET formadepago.id_FormaDePago = formadepago.id_FormaDePago - 10000000 + @idFormaDePagoInc
  WHERE formadepago.id_FormaDePago > 10000000;
-- gasto
UPDATE   gasto
  SET gasto.id_Empresa = gasto.id_Empresa - 10000000 + @idEmpresaInc
  WHERE gasto.id_Empresa > 10000000;
UPDATE   gasto
  SET gasto.id_FormaDePago = gasto.id_FormaDePago - 10000000 + @idFormaDePagoInc
  WHERE gasto.id_FormaDePago > 10000000;
UPDATE   gasto
  SET gasto.id_Gasto = gasto.id_Gasto - 10000000 + @idGastoInc
  WHERE gasto.id_Gasto > 10000000;
UPDATE   gasto
  SET gasto.id_Usuario = gasto.id_Usuario - 10000000 + @idUsuarioInc
  WHERE gasto.id_Usuario > 10000000;
-- item carrito compra??
-- localidad
UPDATE   localidad
  SET localidad.id_Localidad = localidad.id_Localidad - 10000000 + @idLocalidadInc
  WHERE localidad.id_Localidad > 10000000;
UPDATE   localidad
  SET localidad.id_Provincia = localidad.id_Provincia - 10000000 + @idProvinciaInc
  WHERE localidad.id_Provincia > 10000000;
-- MEDIDA
UPDATE   medida
  SET medida.id_Empresa = medida.id_Empresa - 10000000 + @idEmpresaInc
  WHERE medida.id_Empresa > 10000000;
UPDATE   medida
  SET medida.id_Medida = medida.id_Medida - 10000000 + @idMedidaInc
  WHERE medida.id_Medida > 10000000;
-- NOTA
UPDATE  nota
  SET nota.idNota = nota.idNota- 10000000 + @idNotaInc
  WHERE nota.idNota > 10000000;
UPDATE  nota
  SET nota.id_Cliente = nota.id_Cliente - 10000000 + @idClienteInc
  WHERE nota.id_Cliente > 10000000;
UPDATE   nota
  SET nota.id_Empresa = nota.id_Empresa - 10000000 + @idEmpresaInc
  WHERE nota.id_Empresa > 10000000;
UPDATE   nota
  SET nota.id_Factura = nota.id_Factura - 10000000 + @idFacturaInc
  WHERE nota.id_Factura > 10000000;
UPDATE   nota
  SET nota.id_Usuario = nota.id_Usuario - 10000000 + @idUsuarioInc
  WHERE nota.id_Usuario > 10000000;
-- NOTA CREDITO
UPDATE   notacredito
  SET notacredito.idNota = notacredito.idNota - 10000000 + @idNotaInc
  WHERE notacredito.idNota > 10000000;
-- NOTA DEBITO
UPDATE   notadebito
  SET notadebito.idNota = notadebito.idNota - 10000000 + @idNotaInc
  WHERE notadebito.idNota > 10000000;
UPDATE   notadebito
  SET notadebito.pagoId =  IF(notadebito.pagoId = null, null, notadebito.pagoId - 10000000 + @idPagoInc)
  WHERE notadebito.pagoId > 10000000;
-- PAGO
UPDATE   pago
  SET pago.id_Empresa = pago.id_Empresa - 10000000 + @idEmpresaInc
  WHERE pago.id_Empresa > 10000000;
UPDATE   pago
  SET pago.id_Factura = pago.id_Factura - 10000000 + @idFacturaInc
  WHERE pago.id_Factura > 10000000;
UPDATE   pago
  SET pago.id_FormaDePago = pago.id_FormaDePago - 10000000 + @idFormaDePagoInc
  WHERE pago.id_FormaDePago > 10000000;
UPDATE   pago
  SET pago.idNota = IF(pago.idNota = null, null, pago.idNota - 10000000 + @idNotaInc)
  WHERE pago.idNota > 10000000;
UPDATE   pago  
  SET pago.id_Pago = pago.id_Pago - 10000000 + @idPagoInc
  WHERE pago.id_Pago > 10000000;
-- PAIS
UPDATE   pais
  SET pais.id_Pais = pais.id_Pais - 10000000 + @idPaisInc
  WHERE pais.id_Pais > 10000000;
-- PEDIDO
UPDATE   pedido
  SET pedido.id_Cliente = pedido.id_Cliente - 10000000 + @idClienteInc
  WHERE pedido.id_Cliente > 10000000;
UPDATE   pedido
  SET pedido.id_Empresa = pedido.id_Empresa - 10000000 + @idEmpresaInc
  WHERE pedido.id_Empresa > 10000000;
UPDATE   pedido
  SET pedido.id_Pedido = pedido.id_Pedido - 10000000 + @idPedidoInc
  WHERE pedido.id_Pedido > 10000000;  
UPDATE   pedido
  SET pedido.id_Usuario = pedido.id_Usuario - 10000000 + @idUsuarioInc
  WHERE pedido.id_Usuario > 10000000;  
-- PRODUCTO
UPDATE   producto
  SET producto.id_Empresa = producto.id_Empresa - 10000000 + @idEmpresaInc
  WHERE producto.id_Empresa > 10000000;  
UPDATE   producto
  SET producto.id_Medida = producto.id_Medida - 10000000 + @idMedidaInc
  WHERE producto.id_Medida > 10000000;
UPDATE   producto
  SET producto.id_Producto = producto.id_Producto - 10000000 + @idProductoInc
  WHERE producto.id_Producto > 10000000;     
UPDATE   producto
  SET producto.id_Proveedor = producto.id_Proveedor - 10000000 + @idProveedorInc
  WHERE producto.id_Proveedor > 10000000;      
UPDATE   producto
  SET producto.id_Rubro = producto.id_Rubro - 10000000 + @idRubroInc
  WHERE producto.id_Rubro > 10000000;  
-- PROVEEDOR
UPDATE   proveedor
  SET proveedor.id_CondicionIVA = proveedor.id_CondicionIVA - 10000000 + @idCondicionIvaInc
  WHERE proveedor.id_CondicionIVA > 10000000;    
UPDATE   proveedor
  SET proveedor.id_Empresa = proveedor.id_Empresa - 10000000 + @idEmpresaInc
  WHERE proveedor.id_Empresa > 10000000;   
UPDATE   proveedor
  SET proveedor.id_Localidad = proveedor.id_Localidad - 10000000 + @idLocalidadInc
  WHERE proveedor.id_Localidad > 10000000;
UPDATE   proveedor
  SET proveedor.id_Proveedor = proveedor.id_Proveedor - 10000000 + @idProveedorInc
  WHERE proveedor.id_Proveedor > 10000000;
-- PROVINCIA
UPDATE   provincia
  SET provincia.id_Pais = provincia.id_Pais - 10000000 + @idPaisInc
  WHERE provincia.id_Pais > 10000000;  
UPDATE   provincia
  SET provincia.id_Provincia = provincia.id_Provincia - 10000000 + @idProvinciaInc
  WHERE provincia.id_Provincia > 10000000;    
-- RENGLON CUENTA CORRIENTE
UPDATE   rengloncuentacorriente
  SET rengloncuentacorriente.idCuentaCorriente = rengloncuentacorriente.idCuentaCorriente - 10000000 + @idCuentaCorrienteInc
  WHERE rengloncuentacorriente.idCuentaCorriente > 10000000;
UPDATE   rengloncuentacorriente
  SET rengloncuentacorriente.id_Factura = IF(rengloncuentacorriente.id_Factura = null, null, rengloncuentacorriente.id_Factura - 10000000 + @idFacturaInc)
  WHERE rengloncuentacorriente.id_Factura > 10000000;   
UPDATE   rengloncuentacorriente
  SET rengloncuentacorriente.idNota = IF(rengloncuentacorriente.idNota = null, null, rengloncuentacorriente.idNota - 10000000 + @idNotaInc)
  WHERE rengloncuentacorriente.idNota > 10000000;
UPDATE   rengloncuentacorriente
  SET rengloncuentacorriente.id_Pago = IF(rengloncuentacorriente.id_Pago = null, null, rengloncuentacorriente.id_Pago - 10000000 + @idPagoInc)
  WHERE rengloncuentacorriente.id_Pago > 10000000;  
UPDATE   rengloncuentacorriente
  SET rengloncuentacorriente.idRenglonCuentaCorriente = IF(rengloncuentacorriente.idRenglonCuentaCorriente = null, null, rengloncuentacorriente.idRenglonCuentaCorriente - 10000000 + @idRenglonCuentaCorrienteInc)
  WHERE rengloncuentacorriente.idRenglonCuentaCorriente > 10000000;
UPDATE   rengloncuentacorriente
  SET rengloncuentacorriente.idMovimiento = rengloncuentacorriente.idMovimiento - 10000000 + @idPagoInc
  WHERE rengloncuentacorriente.idMovimiento > 10000000 AND rengloncuentacorriente.tipoMovimiento = "PAGO";  
UPDATE   rengloncuentacorriente
  SET rengloncuentacorriente.idMovimiento = rengloncuentacorriente.idMovimiento - 10000000 + @idGastoInc
  WHERE rengloncuentacorriente.idMovimiento > 10000000 AND rengloncuentacorriente.tipoMovimiento = "GASTO";  
UPDATE   rengloncuentacorriente
  SET rengloncuentacorriente.idMovimiento = rengloncuentacorriente.idMovimiento - 10000000 + @idFacturaInc
  WHERE rengloncuentacorriente.idMovimiento > 10000000 AND rengloncuentacorriente.tipoMovimiento = "VENTA";  
UPDATE   rengloncuentacorriente
  SET rengloncuentacorriente.idMovimiento = rengloncuentacorriente.idMovimiento - 10000000 + @idNotaInc
  WHERE rengloncuentacorriente.idMovimiento > 10000000 AND rengloncuentacorriente.tipoMovimiento = "CREDITO";  
UPDATE   rengloncuentacorriente
  SET rengloncuentacorriente.idMovimiento = rengloncuentacorriente.idMovimiento - 10000000 + @idNotaInc
  WHERE rengloncuentacorriente.idMovimiento > 10000000 AND rengloncuentacorriente.tipoMovimiento = "DEBITO";  
-- RENGLON FACTURA
UPDATE   renglonfactura 
  SET renglonfactura.id_Factura = renglonfactura.id_Factura - 10000000 + @idFacturaInc
  WHERE renglonfactura.id_Factura > 10000000;
UPDATE   renglonfactura 
  SET renglonfactura.id_ProductoItem = renglonfactura.id_ProductoItem - 10000000 + @idProductoInc
  WHERE renglonfactura.id_ProductoItem > 10000000;  
UPDATE   renglonfactura 
  SET renglonfactura.id_RenglonFactura = renglonfactura.id_RenglonFactura - 10000000 + @idRenglonFacturaInc
  WHERE renglonfactura.id_RenglonFactura > 10000000; 
-- RENGLON NOTA CREDITO
UPDATE   renglonnotacredito
  SET renglonnotacredito.idNota = renglonnotacredito.idNota - 10000000 + @idNotaInc
  WHERE renglonnotacredito.idNota > 10000000;
UPDATE   renglonnotacredito
  SET renglonnotacredito.idProductoItem = renglonnotacredito.idProductoItem - 10000000 + @idProductoItemInc
  WHERE renglonnotacredito.idProductoItem > 10000000;  
UPDATE   renglonnotacredito
  SET renglonnotacredito.idRenglonNotaCredito = renglonnotacredito.idRenglonNotaCredito - 10000000 + @idRenglonNotaCreditoInc
  WHERE renglonnotacredito.idRenglonNotaCredito > 10000000;
-- RENGLON NOTA DEBITO
UPDATE   renglonnotadebito
  SET renglonnotadebito.idNota = renglonnotadebito.idNota - 10000000 + @idNotaInc
  WHERE renglonnotadebito.idNota > 10000000;
UPDATE   renglonnotadebito
  SET renglonnotadebito.idRenglonNotaDebito = renglonnotadebito.idRenglonNotaDebito - 10000000 + @idRenglonNotaDebitoInc
  WHERE renglonnotadebito.idRenglonNotaDebito > 10000000;
-- RENGLON PEDIDO
UPDATE   renglonpedido
  SET renglonpedido.id_Pedido = renglonpedido.id_Pedido - 10000000 + @idPedidoInc
  WHERE renglonpedido.id_Pedido > 10000000;
UPDATE   renglonpedido
  SET renglonpedido.id_Producto = renglonpedido.id_Producto - 10000000 + @idProductoInc
  WHERE renglonpedido.id_Producto > 10000000;
UPDATE   renglonpedido
  SET renglonpedido.id_RenglonPedido = renglonpedido.id_RenglonPedido - 10000000 + @idRenglonPedidoInc
  WHERE renglonpedido.id_RenglonPedido > 10000000;  
-- ROL
UPDATE   rol
  SET rol.id_Usuario = rol.id_Usuario - 10000000 + @idUsuarioInc
  WHERE rol.id_Usuario > 10000000;
-- RUBRO
UPDATE   rubro
  SET rubro.id_Empresa = rubro.id_Empresa - 10000000 + @idEmpresaInc
  WHERE rubro.id_Empresa > 10000000;
UPDATE   rubro
  SET rubro.id_Rubro = rubro.id_Rubro - 10000000 + @idRubroInc
  WHERE rubro.id_Rubro > 10000000;    
-- TRANSPORTISTA
UPDATE   transportista
  SET transportista.id_Empresa = transportista.id_Empresa - 10000000 + @idEmpresaInc
  WHERE transportista.id_Empresa > 10000000;
UPDATE   transportista
  SET transportista.id_Localidad = transportista.id_Localidad - 10000000 + @idLocalidadInc
  WHERE transportista.id_Localidad > 10000000;
UPDATE   transportista
  SET transportista.id_Transportista = transportista.id_Transportista - 10000000 + @idTransportistaInc
  WHERE transportista.id_Transportista > 10000000;
-- USUARIO  
UPDATE   usuario
  SET usuario.id_Usuario = usuario.id_Usuario - 10000000 + @idUsuarioInc
  WHERE usuario.id_Usuario > 10000000;
  
ALTER TABLE caja AUTO_INCREMENT = 0;
ALTER TABLE cliente AUTO_INCREMENT = 0;
ALTER TABLE condicioniva AUTO_INCREMENT = 0;
ALTER TABLE configuraciondelsistema AUTO_INCREMENT = 0;
ALTER TABLE cuentacorriente AUTO_INCREMENT = 0;
ALTER TABLE empresa AUTO_INCREMENT = 0;
ALTER TABLE factura AUTO_INCREMENT = 0;
ALTER TABLE formadepago AUTO_INCREMENT = 0;
ALTER TABLE gasto AUTO_INCREMENT = 0;
ALTER TABLE localidad AUTO_INCREMENT = 0;
ALTER TABLE medida AUTO_INCREMENT = 0;
ALTER TABLE nota AUTO_INCREMENT = 0;
ALTER TABLE pago AUTO_INCREMENT = 0;
ALTER TABLE pais AUTO_INCREMENT = 0;
ALTER TABLE pedido AUTO_INCREMENT = 0;
ALTER TABLE producto AUTO_INCREMENT = 0;
ALTER TABLE proveedor AUTO_INCREMENT = 0;
ALTER TABLE provincia AUTO_INCREMENT = 0;
ALTER TABLE rengloncuentacorriente AUTO_INCREMENT = 0;
ALTER TABLE renglonfactura AUTO_INCREMENT = 0;
ALTER TABLE renglonnotacredito AUTO_INCREMENT = 0;
ALTER TABLE renglonnotadebito AUTO_INCREMENT = 0;
ALTER TABLE renglonpedido AUTO_INCREMENT = 0;
ALTER TABLE rubro AUTO_INCREMENT = 0;
ALTER TABLE transportista AUTO_INCREMENT = 0;
ALTER TABLE usuario AUTO_INCREMENT = 0;

SET foreign_key_checks = 1;
SET UNIQUE_CHECKS = 1; 
SET SQL_SAFE_UPDATES = 1;
