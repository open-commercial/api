package org.opencommercial.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.opencommercial.model.TipoDeOperacion;
import org.opencommercial.model.criteria.BusquedaProveedorCriteria;
import org.opencommercial.model.Proveedor;

public interface ProveedorService {

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
