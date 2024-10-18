package sic.service;

import com.querydsl.core.BooleanBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sic.modelo.*;
import sic.modelo.criteria.BusquedaRemitoCriteria;
import sic.modelo.dto.NuevoRemitoDTO;

import java.util.List;

public interface RemitoService {

  Remito getRemitoPorId(long idRemito);

  Remito crearRemitoDeFacturasVenta(NuevoRemitoDTO nuevoRemitoDTO, long idUsuario);

  void eliminar(long idRemito);

  List<RenglonRemito> construirRenglonesDeRemito(NuevoRemitoDTO nuevoRemitoDTO);

  long getSiguienteNumeroRemito(Long nroSerie);

  List<RenglonRemito> getRenglonesDelRemito(long idRemito);

  Page<Remito> buscarRemito(BusquedaRemitoCriteria criteria);

  Pageable getPageable(Integer pagina, String ordenarPor, String sentido);

  BooleanBuilder getBuilder(BusquedaRemitoCriteria criteria);

  byte[] getReporteRemito(long idRemito);

  void validarReglasDeNegocio(List<FacturaVenta> facturas, long[] idFacturaVenta);
}
