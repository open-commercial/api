package sic.service;

import java.util.List;
import sic.modelo.Pais;
import sic.modelo.Provincia;

public interface IProvinciaService {
    
    Provincia getProvinciaPorId(Long id_Provincia);

    void actualizar(Provincia provincia);

    void eliminar(long idProvincia);

    Provincia getProvinciaPorNombre(String nombre, Pais pais);

    List<Provincia> getProvinciasDelPais(Pais pais);

    Provincia guardar(Provincia provincia);
    
}
