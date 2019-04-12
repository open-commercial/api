package sic.service;

import org.springframework.data.domain.Page;
import sic.modelo.*;

import java.util.List;

public interface IUbicacionService {

  Ubicacion getUbicacionPorId(long idUbicacion);

  Page<Localidad> buscar(BusquedaLocalidadCriteria criteria);

  Ubicacion guardaUbicacionEmpresa(
    Ubicacion ubicacion,
    Empresa empresa);

  Ubicacion guardar(
    Ubicacion ubicacion);

  void actualizar(Ubicacion ubicacion);

  Localidad getLocalidadPorId(Long idLocalidad);

  Localidad getLocalidadPorNombre(String nombre, Provincia provincia);

  Localidad getLocalidadPorCodigoPostal(String codigoPostal);

  List<Localidad> getLocalidadesDeLaProvincia(Provincia provincia);

  Provincia getProvinciaPorId(Long idProvincia);

  List<Provincia> getProvincias();

  void actualizarLocalidad(Localidad localidad);

  void validarOperacion(TipoDeOperacion operacion, Localidad localidad);
}
