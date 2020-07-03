package sic.service;

import java.util.List;
import sic.modelo.Sucursal;
import sic.modelo.TipoDeOperacion;

public interface ISucursalService {

  Sucursal getSucursalPorId(Long idSucursal);

  Sucursal getSucursalPredeterminada();

  void actualizar(Sucursal sucursalParaActualizar, Sucursal sucursalPersistida);

  void eliminar(Long idSucursal);

  Sucursal getSucursalPorIdFiscal(Long idFiscal);

  void validarReglasDeNegocio(TipoDeOperacion operacion, Sucursal sucursal);

  Sucursal getSucursalPorNombre(String nombre);

  List<Sucursal> getSucusales(boolean puntoDeRetiro);

  Sucursal guardar(Sucursal sucursal);

  String guardarLogo(long idSucursal, byte[] imagen);
}
