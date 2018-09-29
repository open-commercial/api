package sic.service;

import java.util.List;

import org.springframework.data.domain.Page;
import sic.modelo.BusquedaProveedorCriteria;
import sic.modelo.Empresa;
import sic.modelo.Proveedor;

public interface IProveedorService {

  Proveedor getProveedorPorId(Long idProveedor);

  void actualizar(Proveedor proveedor);

  Page<Proveedor> buscarProveedores(BusquedaProveedorCriteria criteria);

  void eliminar(long idProveedor);

  Proveedor getProveedorPorCodigo(String codigo, Empresa empresa);

  Proveedor getProveedorPorIdFiscal(String idFiscal, Empresa empresa);

  Proveedor getProveedorPorRazonSocial(String razonSocial, Empresa empresa);

  List<Proveedor> getProveedores(Empresa empresa);

  Proveedor guardar(Proveedor proveedor);
}
