package sic.service;

import sic.modelo.dto.NuevaOrdenDePagoDTO;
import java.util.List;

public interface IPaymentService {

  boolean isServicioConfigurado();

  List<String> getNuevaPreferenceParams(long idUsuario, NuevaOrdenDePagoDTO nuevaOrdenDeCompra, String origin);

  void crearComprobantePorNotificacion(long idPayment);

  void devolverPago(long idPayment);
}
