package sic.service;

import sic.modelo.Usuario;
import sic.modelo.dto.NuevoPagoMercadoPagoDTO;

public interface IMercadoPagoService {

  void crearNuevoPago(NuevoPagoMercadoPagoDTO nuevoPagoMercadoPagoDTO, Usuario usuario);

  void crearComprobantePorNotificacion(String idPayment);

  void devolverPago(String idPayment);
}
