SET SQL_SAFE_UPDATES = 0;
SET foreign_key_checks = 0;
SET UNIQUE_CHECKS=0; 

SET @idCajaInc = (SELECT max(id_Caja) FROM sic.caja);
SET @idClienteInc = (SELECT max(id_Cliente) FROM sic.cliente);
SET @idCondicionIvaInc = (SELECT max(id_CondicionIVA) FROM sic.condicioniva);
SET @idConfiguraciionDelSistemaInc = (SELECT max(id_ConfiguracionDelSistema) FROM sic.configuraciondelsistema);
SET @idCuentaCorrienteInc = (SELECT max(idCuentaCorriente) FROM sic.cuentacorriente);
SET @idEmpresaInc = (SELECT max(id_Empresa) FROM sic.empresa);
SET @idFacturaInc = (SELECT max(id_Factura) FROM sic.factura);
SET @idFormaDePagoInc = (SELECT max(id_FormaDePago) FROM sic.formadepago);
SET @idGastoInc = (SELECT max(id_Gasto) FROM sic.gasto);
SET @idLocalidadInc = (SELECT max(id_Localidad) FROM sic.localidad);
SET @idMedidaInc = (SELECT max(id_Medida) FROM sic.medida);
SET @idNotaInc = (SELECT max(idNota) FROM sic.nota);
SET @idPagoInc = (SELECT max(id_Pago) FROM sic.pago);
SET @idPaisInc = (SELECT max(id_Pais) FROM sic.pais);
SET @idPedidoInc = (SELECT max(id_Pedido) FROM sic.pedido);
SET @idProductoInc = (SELECT max(id_Producto) FROM sic.producto);
SET @idProveedorInc = (SELECT max(id_Proveedor) FROM sic.proveedor);
SET @idPedidoInc = (SELECT max(id_Pedido) FROM sic.pedido);
SET @idProductoInc = (SELECT max(id_Producto) FROM sic.producto);
SET @idProveedorInc = (SELECT max(id_Proveedor) FROM sic.proveedor);
SET @idProvinciaInc = (SELECT max(id_Provincia) FROM sic.provincia);
SET @idRenglonCuentaCorrienteInc = (SELECT max(idRenglonCuentaCorriente) FROM sic.rengloncuentacorriente);
SET @idRenglonFacturaInc = (SELECT max(id_RenglonFactura) FROM sic.renglonfactura);
SET @idRenglonNotaCreditoInc = (SELECT max(idRenglonNotaCredito) FROM sic.renglonnotacredito);
SET @idRenglonNotaDebitoInc = (SELECT max(idRenglonNotaDebito) FROM sic.renglonnotadebito);
SET @idRenglonPedidoInc = (SELECT max(id_RenglonPedido) FROM sic.renglonpedido);
SET @idRolInc = (SELECT max(id_Usuario) FROM sic.rol);
SET @idRubroInc = (SELECT max(id_Rubro) FROM sic.rubro);
SET @idTransportistaInc = (SELECT max(id_Transportista) FROM sic.transportista);
SET @idUsuarioInc = (SELECT max(id_Usuario) FROM sic.usuario);
SET @idUsuarioCierraInc = (SELECT max(id_UsuarioCierra) FROM sic.caja);

-- Empresa
UPDATE   empresa
  SET empresa.id_Empresa = empresa.id_Empresa + @idEmpresaInc;
UPDATE   empresa
  SET empresa.id_CondicionIVA = empresa.id_CondicionIVA + @idCondicionIVAInc;
UPDATE   empresa
  SET empresa.id_Localidad = empresa.id_Localidad + @idLocalidadInc;  
-- Caja  
UPDATE   caja
  SET caja.id_Caja = caja.id_Caja + @idCajaInc;
UPDATE   caja
  SET caja.id_Empresa = caja.id_Empresa + @idEmpresaInc;
UPDATE   caja
  SET caja.id_Usuario = caja.id_Usuario + @idUsuarioInc;
UPDATE   caja
  SET caja.id_UsuarioCierra = caja.id_UsuarioCierra + @idUsuarioCierraInc;
-- Cliente  
UPDATE   cliente
  SET cliente.id_Cliente = cliente.id_Cliente + @idClienteInc;
UPDATE   cliente
  SET cliente.id_CondicionIVA = cliente.id_CondicionIVA + @idCondicionIVAInc;
UPDATE   cliente
  SET cliente.id_Empresa = cliente.id_Empresa + @idEmpresaInc;
UPDATE   cliente
  SET cliente.id_Localidad = cliente.id_Localidad + @idLocalidadInc;
-- viajante y eso?
-- CondicionIVA  
UPDATE   condicioniva
  SET condicioniva.id_CondicionIVA = condicioniva.id_CondicionIVA + @idCondicionIVAInc;
-- Cuenta Corriente
UPDATE   cuentacorriente
  SET cuentacorriente.idCuentaCorriente = cuentacorriente.idCuentaCorriente + @idCuentaCorrienteInc;
UPDATE   cuentacorriente
  SET cuentacorriente.id_Cliente = cuentacorriente.id_Cliente + @idClienteInc;
UPDATE   cuentacorriente
  SET cuentacorriente.id_Empresa = cuentacorriente.id_Empresa + @idEmpresaInc;
-- CONFIGURACION DEL SISTEMA
UPDATE   configuraciondelsistema
  SET configuraciondelsistema.id_ConfiguracionDelSistema = configuraciondelsistema.id_ConfiguracionDelSistema + 1000000;  
UPDATE   configuraciondelsistema
  SET configuraciondelsistema.id_Empresa = configuraciondelsistema.id_Empresa + @idEmpresaInc; 
-- FACTURA
UPDATE   factura
  SET factura.id_Factura = factura.id_Factura + @idFacturaInc;
UPDATE   factura
  SET factura.id_Pedido = factura.id_Pedido + @idPedidoInc;
UPDATE   factura
  SET factura.id_Empresa = factura.id_Empresa + @idEmpresaInc;
UPDATE   factura
  SET factura.id_Transportista = factura.id_Transportista + @idTransportistaInc;
-- Factura Compra
UPDATE   facturacompra
  SET facturacompra.id_Factura = facturacompra.id_Factura + @idFacturaInc;
UPDATE   facturacompra
  SET facturacompra.id_Proveedor = facturacompra.id_Proveedor + @idProveedorIna;
-- Factura Venta
UPDATE   facturaventa
  SET facturaventa.id_Cliente = facturaventa.id_Cliente + @idClienteInc;
UPDATE   facturaventa
  SET facturaventa.id_Factura = facturaventa.id_Factura + @idFacturaInc;
UPDATE   facturaventa
  SET facturaventa.id_Usuario = facturaventa.id_Usuario + @idUsuarioInc;
-- Forma de Pago
UPDATE   formadepago
  SET formadepago.id_Empresa = formadepago.id_Empresa+ @idEmpresaInc;
UPDATE   formadepago
  SET formadepago.id_FormaDePago = formadepago.id_FormaDePago + @idFormaDePagoInc;
-- gasto
UPDATE   gasto
  SET gasto.id_Empresa = gasto.id_Empresa + @idEmpresaInc;
UPDATE   gasto
  SET gasto.id_FormaDePago = gasto.id_FormaDePago + @idFormaDePagoInc;
UPDATE   gasto
  SET gasto.id_Gasto = gasto.id_Gasto + @idGastoInc;
UPDATE   gasto
  SET gasto.id_Usuario = gasto.id_Usuario + @idUsuarioInc;
-- item carrito compra??
-- localidad
UPDATE   localidad
  SET localidad.id_Localidad = localidad.id_Localidad + @idLocalidadInc;
UPDATE   localidad
  SET localidad.id_Provincia = localidad.id_Provincia + @idProvinciaInc;
-- MEDIDA
UPDATE   medida
  SET medida.id_Empresa = medida.id_Empresa + @idEmpresaInc;
UPDATE   medida
  SET medida.id_Medida = medida.id_Medida + @idMedidaInc;
-- NOTA
UPDATE   nota
  SET nota.id_Cliente = nota.id_Cliente + @idClienteInc;
UPDATE   nota
  SET nota.id_Empresa = nota.id_Empresa + @idEmpresaInc;
UPDATE   nota
  SET nota.id_Factura = nota.id_Factura + @idFacturaInc;
UPDATE   nota
  SET nota.id_Usuario = nota.id_Usuario + @idNotaInc;
-- NOTA CREDITO
UPDATE   notacredito
  SET notacredito.idNota = notacredito.idNota + @idNotaInc;
-- NOTA DEBITO
UPDATE   notadebito
  SET notadebito.idNota = notadebito.idNota + @idNotaInc;
UPDATE   notadebito
  SET notadebito.pagoId =  IF(notadebito.pagoId = null, null, notadebito.pagoId + @idPagoInc);
-- PAGO
UPDATE   pago
  SET pago.id_Empresa = pago.id_Empresa + @idEmpresaInc;
UPDATE   pago
  SET pago.id_Factura = pago.id_Factura + @idFacturaInc;
UPDATE   pago
  SET pago.id_FormaDePago = pago.id_FormaDePago + @idFormaDePagoInc;
UPDATE   pago
  SET pago.idNota = IF(pago.idNota = null, null, pago.idNota + @idNotaInc);
UPDATE   pago  
  SET pago.id_Pago = pago.id_Pago + @idPagoInc;
-- PAIS
UPDATE   pais
  SET pais.id_Pais = pais.id_Pais + @idPaisInc;
-- PEDIDO
UPDATE   pedido
  SET pedido.id_Cliente = pedido.id_Cliente + @idClienteInc;
UPDATE   pedido
  SET pedido.id_Empresa = pedido.id_Empresa + @idEmpresaInc;
UPDATE   pedido
  SET pedido.id_Pedido = pedido.id_Pedido + @idPedidoInc;  
UPDATE   pedido
  SET pedido.id_Usuario = pedido.id_Usuario + @idUsuarioInc;  
-- PRODUCTO
UPDATE   producto
  SET producto.id_Empresa = producto.id_Empresa + @idEmpresaInc;  
UPDATE   producto
  SET producto.id_Medida = producto.id_Medida + @idMedidaInc;
UPDATE   producto
  SET producto.id_Producto = producto.id_Producto + @idProductoInc;     
UPDATE   producto
  SET producto.id_Proveedor = producto.id_Proveedor + @idProveedorInc;      
UPDATE   producto
  SET producto.id_Rubro = producto.id_Rubro + @idRubroInc;  
-- PROVEEDOR
UPDATE   proveedor
  SET proveedor.id_CondicionIVA = proveedor.id_CondicionIVA + @idCondicionIvaInc;    
UPDATE   proveedor
  SET proveedor.id_Empresa = proveedor.id_Empresa + @idEmpresaInc;   
UPDATE   proveedor
  SET proveedor.id_Localidad = proveedor.id_Localidad + @idLocalidadInc;
UPDATE   proveedor
  SET proveedor.id_Proveedor = proveedor.id_Proveedor + @idProveedorInc;
-- PROVINCIA
UPDATE   provincia
  SET provincia.id_Pais = provincia.id_Pais + @idPaisInc;  
UPDATE   provincia
  SET provincia.id_Provincia = provincia.id_Provincia + @idProvinciaInc;    
-- RENGLON CUENTA CORRIENTE
UPDATE   rengloncuentacorriente
  SET rengloncuentacorriente.idCuentaCorriente = rengloncuentacorriente.idCuentaCorriente + @idCuentaCorrienteInc;
UPDATE   rengloncuentacorriente
  SET rengloncuentacorriente.id_Factura = IF(rengloncuentacorriente.id_Factura = null, null, rengloncuentacorriente.id_Factura + @idFacturaInc);
UPDATE   rengloncuentacorriente
  SET rengloncuentacorriente.idMovimiento = rengloncuentacorriente.idMovimiento + @idMovimientoInc;  
UPDATE   rengloncuentacorriente
  SET rengloncuentacorriente.idNota = IF(rengloncuentacorriente.idNota = null, null, rengloncuentacorriente.idNota + @idNotaInc);
UPDATE   rengloncuentacorriente
  SET rengloncuentacorriente.id_Pago = IF(rengloncuentacorriente.id_Pago = null, null, rengloncuentacorriente.id_Pago + @idPagoInc);  
UPDATE   rengloncuentacorriente
  SET rengloncuentacorriente.idRenglonCuentaCorriente = IF(rengloncuentacorriente.idRenglonCuentaCorriente = null, null, rengloncuentacorriente.idRenglonCuentaCorriente + @idRenglonCuentaCorrienteInc);
-- RENGLON FACTURA
UPDATE   renglonfactura 
  SET renglonfactura.id_Factura = renglonfactura.id_Factura + @idFacturaInc;
UPDATE   renglonfactura 
  SET renglonfactura.id_ProductoItem = renglonfactura.id_ProductoItem + @idProductoInc;  
UPDATE   renglonfactura 
  SET renglonfactura.id_RenglonFactura = renglonfactura.id_RenglonFactura + @idRenglonFacturaInc; 
-- RENGLON NOTA CREDITO
UPDATE   renglonnotacredito
  SET renglonnotacredito.idNota = renglonnotacredito.idNota + @idNotaInc;
UPDATE   renglonnotacredito
  SET renglonnotacredito.idProductoItem = renglonnotacredito.idProductoItem + @idProductoItem;  
UPDATE   renglonnotacredito
  SET renglonnotacredito.idRenglonNotaCredito = renglonnotacredito.idRenglonNotaCredito + @idRenglonNotaCreditoInc;
-- RENGLON NOTA DEBITO
UPDATE   renglonnotadebito
  SET renglonnotadebito.idNota = renglonnotadebito.idNota + @idNotaInc;
UPDATE   renglonnotadebito
  SET renglonnotadebito.idRenglonNotaDebito = renglonnotadebito.idRenglonNotaDebito + @idRenglonNotaDebitoInc;
-- RENGLON PEDIDO
UPDATE   renglonpedido
  SET renglonpedido.id_Pedido = renglonpedido.id_Pedido + @idPedidoInc;
UPDATE   renglonpedido
  SET renglonpedido.id_Producto = renglonpedido.id_Producto + @idProductoInc;
UPDATE   renglonpedido
  SET renglonpedido.id_RenglonPedido = renglonpedido.id_RenglonPedido + @idRenglonPedidoInc;  
-- ROL
UPDATE   rol
  SET rol.id_Usuario = rol.id_Usuario + @idUsuarioInc;
-- RUBRO
UPDATE   rubro
  SET rubro.id_Empresa = rubro.id_Empresa + @idEmpresaInc;
UPDATE   rubro
  SET rubro.id_Rubro = rubro.id_Rubro + @idRubroInc;    
-- TRANSPORTISTA
UPDATE   transportista
  SET transportista.id_Empresa = transportista.id_Empresa + @idEmpresaInc;
UPDATE   transportista
  SET transportista.id_Localidad = transportista.id_Localidad + @idLocalidadInc;
UPDATE   transportista
  SET transportista.id_Transportista = transportista.id_Transportista + @idTransportistaInc;
-- USUARIO  
UPDATE   usuario
  SET usuario.id_Usuario = usuario.id_Usuario + @idUsuarioInc;

SET foreign_key_checks = 1;
SET UNIQUE_CHECKS=1; 
SET SQL_SAFE_UPDATES = 1;