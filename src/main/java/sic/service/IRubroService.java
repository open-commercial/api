package sic.service;

import java.util.List;
import sic.modelo.Empresa;
import sic.modelo.Rubro;

public interface IRubroService {

    Rubro getRubroPorId(Long idRubro);
    
    void actualizar(Rubro rubro);

    void eliminar(long idRubro);

    Rubro getRubroPorNombre(String nombre, Empresa empresa);

    List<Rubro> getRubros(Empresa empresa);

    Rubro guardar(Rubro rubro);
    
}
