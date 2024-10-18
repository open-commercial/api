package sic.service;

import sic.modelo.ConfiguracionSucursal;

public interface ConfiguracionSucursalService {

  void actualizar(ConfiguracionSucursal configuracionSucursalPorActualizar);

  ConfiguracionSucursal getConfiguracionSucursalPorId(long idConfiguracionSucursal);

  ConfiguracionSucursal guardar(ConfiguracionSucursal configuracionSucursal);

  void eliminar(ConfiguracionSucursal configuracionSucursal);

  void validarReglasDeNegocio(ConfiguracionSucursal configuracionSucursal);

  int getCantidadMaximaDeRenglonesPorIdSucursal(long idSucursal);

  boolean isFacturaElectronicaHabilitada(long idSucursal);
}
