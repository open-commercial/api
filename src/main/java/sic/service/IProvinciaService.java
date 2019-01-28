package sic.service;

import sic.modelo.Provincia;

import java.util.List;

public interface IProvinciaService {
    
    Provincia getProvinciaPorId(Long id_Provincia);

    void actualizar(Provincia provincia);

    void eliminar(long idProvincia);

    Provincia getProvinciaPorNombre(String nombre);

    Provincia guardar(Provincia provincia);

    List<Provincia> getProvincias();
    
}
