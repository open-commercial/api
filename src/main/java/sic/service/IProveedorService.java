package sic.service;

import java.util.List;

import org.springframework.data.domain.Page;
import sic.modelo.TipoDeOperacion;
import sic.modelo.criteria.BusquedaProveedorCriteria;
import sic.modelo.Proveedor;

public interface IProveedorService {

  Proveedor getProveedorNoEliminadoPorId(long idProveedor);

  void actualizar(Proveedor proveedor);

  Page<Proveedor> buscarProveedores(BusquedaProveedorCriteria criteria);

  void eliminar(long idProveedor);

  Proveedor getProveedorPorRazonSocial(String razonSocial);

  void validarReglasDeNegocio(TipoDeOperacion operacion, Proveedor proveedor);

  List<Proveedor> getProveedores();

  Proveedor guardar(Proveedor proveedor);

  String generarNroDeProveedor();
}
