package sic.service;

import sic.modelo.ConfiguracionSucursal;
import sic.modelo.Sucursal;

public interface IConfiguracionSucursalService {

  void actualizar(ConfiguracionSucursal configuracionSucursal);

  ConfiguracionSucursal getConfiguracionSucursal(Sucursal sucursal);

  ConfiguracionSucursal getConfiguracionSucursalPorId(long idConfiguracionSucursal);

  ConfiguracionSucursal guardar(ConfiguracionSucursal configuracionSucursal);

  void eliminar(ConfiguracionSucursal configuracionSucursal);

  void validarReglasDeNegocio(ConfiguracionSucursal configuracionSucursal);

  int getCantidadMaximaDeRenglonesPorIdSucursal(long idSucursal);

  boolean isFacturaElectronicaHabilitada(long idSucursal);
}
