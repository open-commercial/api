package sic.service;

import sic.modelo.*;

import java.util.List;

public interface IUbicacionService {

  Ubicacion getUbicacionPorId(long idUbicacion);

  Ubicacion guardarUbicacionDeFacturacionCliente(
      Ubicacion ubicacion,
      String nombreLocalidad,
      String codigoPostal,
      String nombreProvincia,
      Cliente cliente);

  Ubicacion guardarUbicacionDeEnvioCliente(
      Ubicacion ubicacion,
      String nombreLocalidad,
      String codigoPostal,
      String nombreProvincia,
      Cliente cliente);

  Ubicacion guardaUbicacionEmpresa(
      Ubicacion ubicacion,
      String nombreLocalidad,
      String codigoPostal,
      String nombreProvincia,
      Empresa empresa);

  Ubicacion guardaUbicacionProveedor(
      Ubicacion ubicacion,
      String nombreLocalidad,
      String codigoPostal,
      String nombreProvincia,
      Proveedor proveedor);

  Ubicacion guardarUbicacionTransportista(
      Ubicacion ubicacion,
      String nombreLocalidad,
      String codigoPostal,
      String nombreProvincia,
      Transportista transportista);

  Ubicacion guardar(
      Ubicacion ubicacion, String nombreLocalidad, String codigoPostal, String nombreProvincia);

  void actualizar(Ubicacion ubicacion,
                  String nombreLocalidad,
                  String codigoPostal,
                  String nombreProvincia);

  Localidad getLocalidadPorId(Long idLocalidad);

  Localidad getLocalidadPorNombre(String nombre, Provincia provincia);

  List<Localidad> getLocalidadesDeLaProvincia(Provincia provincia);

  Provincia getProvinciaPorId(Long idProvincia);

  Provincia getProvinciaPorNombre(String nombre);

  List<Provincia> getProvincias();

  Localidad guardarLocalidad(String nombre, String nombreProvincia, String codigoPostal);

  void actualizarLocalidad(Localidad localidad);

  void validarOperacion(TipoDeOperacion operacion, Localidad localidad);
}
