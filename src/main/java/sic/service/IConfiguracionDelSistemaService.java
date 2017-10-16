package sic.service;

import sic.modelo.ConfiguracionDelSistema;
import sic.modelo.Empresa;
import sic.modelo.TipoDeOperacion;

public interface IConfiguracionDelSistemaService {

    void actualizar(ConfiguracionDelSistema cds);

    ConfiguracionDelSistema getConfiguracionDelSistemaPorEmpresa(Empresa empresa);

    ConfiguracionDelSistema getConfiguracionDelSistemaPorId(long id_ConfiguracionDelSistema);

    ConfiguracionDelSistema guardar(ConfiguracionDelSistema cds);
    
    void eliminar(ConfiguracionDelSistema cds);
    
    void validarCds(TipoDeOperacion tipoOperacion, ConfiguracionDelSistema cds);
    
}
