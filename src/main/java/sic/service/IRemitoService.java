package sic.service;

import sic.modelo.*;
import sic.modelo.dto.NuevoRemitoDTO;

import java.util.List;

public interface IRemitoService {

  Remito getRemitoPorId(long idRemito);

  Remito crearRemitoDeFacturaVenta(NuevoRemitoDTO nuevoRemitoDTO, long idUsuario);

  void eliminar(long idRemito);

  List<RenglonRemito> construirRenglonesDeRemito(FacturaVenta facturaVenta);

  long getSiguienteNumeroRemito(TipoDeComprobante tipoDeComprobante, Long nroSerie);

  List<RenglonRemito> getRenglonesDelRemito(long idRemito);
}
