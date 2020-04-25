package sic.service;

import java.util.List;
import sic.modelo.Sucursal;

public interface ISucursalService {

  Sucursal getSucursalPorId(Long idSucursal);

  void actualizar(Sucursal sucursalParaActualizar, Sucursal sucursalPersistida);

  void eliminar(Long idSucursal);

  Sucursal getSucursalPorIdFiscal(Long idFiscal);

  Sucursal getSucursalPorNombre(String nombre);

  List<Sucursal> getSucusales(boolean puntoDeRetiro);

  Sucursal guardar(Sucursal sucursal);

  String guardarLogo(long idSucursal, byte[] imagen);
}
