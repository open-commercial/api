package sic.service;

import sic.modelo.TipoDeOperacion;
import java.util.List;
import sic.modelo.Localidad;
import sic.modelo.Provincia;

public interface ILocalidadService {

    Localidad getLocalidadPorId(Long id_Localidad);
            
    void actualizar(Localidad localidad);

    void eliminar(Long idLocalidad);

    Localidad getLocalidadPorNombre(String nombre, Provincia provincia);

    List<Localidad> getLocalidadesDeLaProvincia(Provincia provincia);

    Localidad guardar(Localidad localidad);

    void validarOperacion(TipoDeOperacion operacion, Localidad localidad);

}
