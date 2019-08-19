package sic.service;

import sic.modelo.ConfiguracionDelSistema;
import sic.modelo.Sucursal;

import javax.validation.Valid;

public interface IConfiguracionDelSistemaService {

  void actualizar(@Valid ConfiguracionDelSistema cds);

  ConfiguracionDelSistema getConfiguracionDelSistemaPorSucursal(Sucursal sucursal);

  ConfiguracionDelSistema getConfiguracionDelSistemaPorId(long idConfiguracionDelSistema);

  ConfiguracionDelSistema guardar(@Valid ConfiguracionDelSistema cds);

  void eliminar(ConfiguracionDelSistema cds);

  void validarOperacion(ConfiguracionDelSistema cds);

  int getCantidadMaximaDeRenglonesPorIdSucursal(long idSucursal);

  boolean isFacturaElectronicaHabilitada(long idSucursal);
}
