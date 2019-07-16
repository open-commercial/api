package sic.service;

import sic.modelo.Usuario;
import sic.modelo.dto.NotificacionMercadoPagoDTO;
import sic.modelo.dto.NuevoPagoMercadoPagoDTO;
import sic.modelo.dto.PagoMercadoPagoDTO;

public interface IPagoMercadoPagoService {

  void crearNuevoRecibo(NuevoPagoMercadoPagoDTO nuevoPagoMercadoPagoDTO, Usuario usuario);

  void crearReciboPorNotificacion(NotificacionMercadoPagoDTO notificacion);

  PagoMercadoPagoDTO recuperarPago(String idPago);

  NuevoPagoMercadoPagoDTO devolverPago(String idPago, Usuario usuario);
}
