package sic.service;

import sic.modelo.TipoDeOperacion;
import java.util.List;
import sic.modelo.CondicionIVA;

public interface ICondicionIVAService {
    
    CondicionIVA getCondicionIVAPorId(long idCondicionIVA);

    void actualizar(CondicionIVA condicionIVA);

    void eliminar(Long idCondicionIVA);

    CondicionIVA getCondicionIVAPorNombre(String nombre);

    List<CondicionIVA> getCondicionesIVA();

    CondicionIVA guardar(CondicionIVA condicionIVA);

    void validarOperacion(TipoDeOperacion operacion, CondicionIVA condicionIVA);

}
