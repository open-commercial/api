package sic.service;

import sic.modelo.Recibo;
import sic.modelo.Usuario;
import sic.modelo.dto.NotificacionMercadoPagoDTO;
import sic.modelo.dto.NuevoPagoMercadoPagoDTO;
import sic.modelo.dto.PagoMercadoPagoDTO;

public interface IPagoMercadoPagoService {

  Recibo crearNuevoRecibo(NuevoPagoMercadoPagoDTO nuevoPagoMercadoPagoDTO, Usuario usuario);

  Recibo crearReciboPorNotificacion(NotificacionMercadoPagoDTO notificacion);

  PagoMercadoPagoDTO recuperarPago(String idPago);

  NuevoPagoMercadoPagoDTO devolverPago(String idPago, Usuario usuario);
}
