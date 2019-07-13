package sic.service;

import sic.modelo.Recibo;
import sic.modelo.Usuario;
import sic.modelo.dto.NuevoPagoMercadoPagoDTO;
import sic.modelo.dto.PagoMercadoPagoDTO;

public interface IPagoMercadoPagoService {

  Recibo crearNuevoPago(NuevoPagoMercadoPagoDTO nuevoPagoMercadoPagoDTO, Usuario usuario);

  PagoMercadoPagoDTO recuperarPago(String idPago);

  NuevoPagoMercadoPagoDTO devolverPago(String idPago, Usuario usuario);

  PagoMercadoPagoDTO[] recuperarPagosPendientesDeClientePorMail(String email);
}
