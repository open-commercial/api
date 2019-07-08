package sic.service;

import sic.modelo.Recibo;
import sic.modelo.dto.PagoMercadoPagoDTO;

public interface IPagoMercadoPagoService {

  Recibo crearNuevoPago(PagoMercadoPagoDTO pagoMercadoPagoDTO);

  PagoMercadoPagoDTO recuperarPago(String idPago);

  PagoMercadoPagoDTO devolverPago(String idPago);
}
