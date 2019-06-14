package sic.service;

import sic.modelo.Cliente;
import sic.modelo.dto.PagoMercadoPagoDTO;

public interface IPagoMercadoPagoService {

  boolean crearNuevoPago(PagoMercadoPagoDTO pagoMercadoPagoDTO, Cliente cliente, Float monto);
}
