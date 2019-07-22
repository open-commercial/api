package sic.service;

import sic.modelo.Usuario;
import sic.modelo.dto.NuevoPagoMercadoPagoDTO;
import sic.modelo.dto.PagoMercadoPagoDTO;

public interface IPagoMercadoPagoService {

  void crearNuevoPago(NuevoPagoMercadoPagoDTO nuevoPagoMercadoPagoDTO, Usuario usuario);

  void crearComprobantePorNotificacion(String idPayment);

  PagoMercadoPagoDTO recuperarPago(String idPayment);
}
