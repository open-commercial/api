package sic.service;

import sic.modelo.ConfiguracionDelSistema;
import sic.modelo.Empresa;
import sic.modelo.TipoDeOperacion;

import javax.validation.Valid;

public interface IConfiguracionDelSistemaService {

    void actualizar(@Valid ConfiguracionDelSistema cds);

    ConfiguracionDelSistema getConfiguracionDelSistemaPorEmpresa(Empresa empresa);

    ConfiguracionDelSistema getConfiguracionDelSistemaPorId(long idConfiguracionDelSistema);

    ConfiguracionDelSistema guardar(@Valid ConfiguracionDelSistema cds);
    
    void eliminar(ConfiguracionDelSistema cds);
    
    void validarOperacion(TipoDeOperacion tipoOperacion, ConfiguracionDelSistema cds);

    int getCantidadMaximaDeRenglonesPorIdEmpresa(long  idEmpresa);

    boolean isFacturaElectronicaHabilitada(long  idEmpresa);
    
}
