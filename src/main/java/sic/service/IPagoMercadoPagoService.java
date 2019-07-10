package sic.service;

import com.mercadopago.core.MPResourceArray;
import sic.modelo.Recibo;
import sic.modelo.dto.PagoMercadoPagoDTO;

import javax.validation.constraints.Email;

public interface IPagoMercadoPagoService {

  Recibo crearNuevoPago(PagoMercadoPagoDTO pagoMercadoPagoDTO);

  PagoMercadoPagoDTO recuperarPago(String idPago);

  PagoMercadoPagoDTO devolverPago(String idPago);

  MPResourceArray recuperarPagosPendientesDeClientePorMail(@Email String email);
}
