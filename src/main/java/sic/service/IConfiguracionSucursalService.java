package sic.service;

import sic.modelo.ConfiguracionSucursal;
import sic.modelo.Sucursal;

import javax.validation.Valid;

public interface IConfiguracionSucursalService {

  void actualizar(@Valid ConfiguracionSucursal configuracionSucursal);

  ConfiguracionSucursal getConfiguracionSucursal(Sucursal sucursal);

  ConfiguracionSucursal getConfiguracionSucursalPorId(long idConfiguracionSucursal);

  ConfiguracionSucursal guardar(@Valid ConfiguracionSucursal configuracionSucursal);

  void eliminar(ConfiguracionSucursal configuracionSucursal);

  void validarOperacion(ConfiguracionSucursal configuracionSucursal);

  int getCantidadMaximaDeRenglonesPorIdSucursal(long idSucursal);

  boolean isFacturaElectronicaHabilitada(long idSucursal);
}
