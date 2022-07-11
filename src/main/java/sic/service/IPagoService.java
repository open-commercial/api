package sic.service;

import sic.modelo.dto.NuevaOrdenDePagoDTO;
import java.util.List;

public interface IPagoService {

    List<String> getNuevaPreferenceParams(
            long idUsuario, NuevaOrdenDePagoDTO nuevaOrdenDeCompra, String origin);

    void crearComprobantePorNotificacion(String idPayment);

    void devolverPago(String idPayment);
}
