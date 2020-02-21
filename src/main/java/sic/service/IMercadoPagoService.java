package sic.service;

import com.mercadopago.exceptions.MPException;
import sic.modelo.dto.NuevoPagoMercadoPagoDTO;

import javax.validation.Valid;

public interface IMercadoPagoService {

  String crearNuevoPago(@Valid NuevoPagoMercadoPagoDTO nuevoPagoMercadoPagoDTO) throws MPException;

  void crearComprobantePorNotificacion(String idPayment);

  void devolverPago(String idPayment);

  void logExceptionMercadoPago(MPException ex);
}
