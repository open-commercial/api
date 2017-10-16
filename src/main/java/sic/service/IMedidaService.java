package sic.service;

import sic.modelo.TipoDeOperacion;
import java.util.List;
import sic.modelo.Empresa;
import sic.modelo.Medida;

public interface IMedidaService {
    
    Medida getMedidaPorId(Long id_Medida);

    void actualizar(Medida medida);

    void eliminar(long idMedida);

    Medida getMedidaPorNombre(String nombre, Empresa empresa);

    List<Medida> getUnidadMedidas(Empresa empresa);

    Medida guardar(Medida medida);

    void validarOperacion(TipoDeOperacion operacion, Medida medida);

}
