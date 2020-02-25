package sic.service;

import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.Preference;
import sic.modelo.dto.NuevoPagoMercadoPagoDTO;

import javax.validation.Valid;

public interface IMercadoPagoService {

  String crearNuevoPago(@Valid NuevoPagoMercadoPagoDTO nuevoPagoMercadoPagoDTO) throws MPException;

  Preference crearNuevaPreferencia(String nombreProducto, int cantidad, float precioUnitario);

  void crearComprobantePorNotificacion(String idPayment);

  void devolverPago(String idPayment);

  void logExceptionMercadoPago(MPException ex);
}
