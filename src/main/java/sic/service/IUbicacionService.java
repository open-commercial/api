package sic.service;

import org.springframework.data.domain.Page;
import sic.modelo.*;

import java.util.List;

public interface IUbicacionService {

  Ubicacion guardar(Ubicacion ubicacion);

  void actualizarUbicacionEnvio(Ubicacion ubicacion, Cliente cliente);

  void actualizarUbicacionFacturacion(Ubicacion ubicacion, Cliente cliente);

  Page<Ubicacion> buscarUbicaciones(BusquedaUbicacionCriteria criteria);

  void eliminarUbicacion(long idUbicacion);

  Ubicacion getUbicacionPorId(long idUbicacion);

  Localidad getLocalidadPorId(Long id_Localidad);

  Localidad getLocalidadPorNombre(String nombre, Provincia provincia);

  List<Localidad> getLocalidadesDeLaProvincia(Provincia provincia);

  Provincia getProvinciaPorId(Long id_Provincia);

  Provincia getProvinciaPorNombre(String nombre);

  List<Provincia> getProvincias();

  Localidad guardarLocalidad(String nombre, String nombreProvincia, String codigoPostal);
}
