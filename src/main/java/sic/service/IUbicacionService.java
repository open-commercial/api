package sic.service;

import org.springframework.data.domain.Page;
import sic.modelo.*;

import java.util.List;

public interface IUbicacionService {

  Ubicacion guardarUbicacionDeFacturacionCliente(Ubicacion ubicacion, Cliente cliente);

  Ubicacion guardarUbicacionDeEnvioCliente(Ubicacion ubicacion, Cliente cliente);

  Ubicacion guardaUbicacionEmpresa(Ubicacion ubicacion, Empresa empresa);

  Ubicacion guardaUbicacionProveedor(Ubicacion ubicacion, Proveedor proveedor);

  Ubicacion guardarUbicacionTransportista(Ubicacion ubicacion, Transportista transportista);

  Ubicacion guardar(Ubicacion ubicacion);

  void actualizar(Ubicacion ubicacion);

  Page<Ubicacion> buscarUbicaciones(BusquedaUbicacionCriteria criteria);

  Ubicacion getUbicacionPorId(long idUbicacion);

  Localidad getLocalidadPorId(Long id_Localidad);

  Localidad getLocalidadPorNombre(String nombre, Provincia provincia);

  List<Localidad> getLocalidadesDeLaProvincia(Provincia provincia);

  Provincia getProvinciaPorId(Long id_Provincia);

  Provincia getProvinciaPorNombre(String nombre);

  List<Provincia> getProvincias();

  Localidad guardarLocalidad(String nombre, String nombreProvincia, String codigoPostal);
}
