package org.opencommercial.service;

import org.opencommercial.model.dto.NuevaOrdenDePagoDTO;
import java.util.List;

public interface PaymentService {

  boolean isServicioConfigurado();

  List<String> getNuevaPreferenceParams(long idUsuario, NuevaOrdenDePagoDTO nuevaOrdenDeCompra, String origin);

  void crearComprobantePorNotificacion(long idPayment);

  void devolverPago(long idPayment);
}
