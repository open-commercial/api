package sic.service;

import java.util.List;
import sic.modelo.Empresa;
import sic.modelo.Rubro;

import javax.validation.Valid;

public interface IRubroService {

    Rubro getRubroPorId(Long idRubro);
    
    void actualizar(@Valid Rubro rubro);

    void eliminar(long idRubro);

    Rubro getRubroPorNombre(String nombre, Empresa empresa);

    List<Rubro> getRubros(Empresa empresa);

    Rubro guardar(@Valid Rubro rubro);
    
}
