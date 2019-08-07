package sic.service;

import sic.modelo.TipoDeOperacion;
import java.util.List;
import sic.modelo.Empresa;
import sic.modelo.Medida;

import javax.validation.Valid;

public interface IMedidaService {
    
    Medida getMedidaNoEliminadaPorId(Long id_Medida);

    void actualizar(@Valid Medida medida);

    void eliminar(long idMedida);

    Medida getMedidaPorNombre(String nombre);

    List<Medida> getUnidadMedidas();

    Medida guardar(@Valid Medida medida);

    void validarOperacion(TipoDeOperacion operacion, Medida medida);

}
