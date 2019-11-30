package sic.service;

import java.util.List;
import sic.modelo.Rubro;

import javax.validation.Valid;

public interface IRubroService {

    Rubro getRubroNoEliminadoPorId(Long idRubro);
    
    void actualizar(@Valid Rubro rubro);

    void eliminar(long idRubro);

    Rubro getRubroPorNombre(String nombre);

    List<Rubro> getRubros();

    Rubro guardar(@Valid Rubro rubro);
    
}
