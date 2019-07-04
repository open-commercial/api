package sic.service;

import com.mercadopago.resources.Payment;
import sic.modelo.dto.PagoMercadoPagoDTO;

public interface IPagoMercadoPagoService {

  boolean crearNuevoPago(PagoMercadoPagoDTO pagoMercadoPagoDTO);

  Payment recuperarPago(String idPago);
}
