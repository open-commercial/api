package sic.service;

import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.Payment;
import sic.modelo.Usuario;
import sic.modelo.dto.NuevoPagoMercadoPagoDTO;

import javax.validation.Valid;

public interface IMercadoPagoService {

  Payment.Status crearNuevoPago(@Valid NuevoPagoMercadoPagoDTO nuevoPagoMercadoPagoDTO, Usuario usuario) throws MPException;

  void crearComprobantePorNotificacion(String idPayment);

  void devolverPago(String idPayment);

  void logExceptionMercadoPago(MPException ex);
}
