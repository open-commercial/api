package sic.service;

import java.util.List;
import sic.modelo.Sucursal;
import sic.modelo.TipoDeOperacion;
import sic.modelo.Ubicacion;
import sic.modelo.dto.NuevaSucursalDTO;

public interface SucursalService {

  Sucursal getSucursalPorId(Long idSucursal);

  Sucursal getSucursalPredeterminada();

  void actualizar(Sucursal sucursalParaActualizar, Sucursal sucursalPersistida, byte[] imagen);

  void eliminar(Long idSucursal);

  Sucursal getSucursalPorIdFiscal(Long idFiscal);

  void validarReglasDeNegocio(TipoDeOperacion operacion, Sucursal sucursal);

  Sucursal getSucursalPorNombre(String nombre);

  List<Sucursal> getSucusales(boolean puntoDeRetiro);

  Sucursal guardar(NuevaSucursalDTO nuevaSucursal, Ubicacion ubicacion, byte[] imagen);

  String guardarLogo(long idSucursal, byte[] imagen);
}
