package sic.service;

import com.mercadopago.exceptions.MPException;
import sic.modelo.dto.MercadoPagoPreferenceDTO;
import sic.modelo.dto.NuevaOrdenDePagoDTO;

public interface IMercadoPagoService {

  MercadoPagoPreferenceDTO crearNuevaPreference(
      long idUsuario, NuevaOrdenDePagoDTO nuevaOrdenDeCompra, String origin);

  void crearComprobantePorNotificacion(String idPayment);

  void devolverPago(String idPayment);

  void logExceptionMercadoPago(MPException ex);
}
