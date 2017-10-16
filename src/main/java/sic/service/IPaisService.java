package sic.service;

import java.util.List;
import sic.modelo.Pais;

public interface IPaisService {
    
    Pais getPaisPorId(Long id_Pais);

    void actualizar(Pais pais);

    void eliminar(Long idPais);

    Pais getPaisPorNombre(String nombre);

    List<Pais> getPaises();

    Pais guardar(Pais pais);
    
}
