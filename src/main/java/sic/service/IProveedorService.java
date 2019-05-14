package sic.service;

import java.util.List;

import org.springframework.data.domain.Page;
import sic.modelo.BusquedaProveedorCriteria;
import sic.modelo.Empresa;
import sic.modelo.Proveedor;

import javax.validation.Valid;

public interface IProveedorService {

  Proveedor getProveedorPorId(Long id_Proveedor);

  void actualizar(@Valid Proveedor proveedor);

  Page<Proveedor> buscarProveedores(BusquedaProveedorCriteria criteria);

  void eliminar(long idProveedor);

  Proveedor getProveedorPorCodigo(String codigo, Empresa empresa);

  Proveedor getProveedorPorIdFiscal(Long idFiscal, Empresa empresa);

  Proveedor getProveedorPorRazonSocial(String razonSocial, Empresa empresa);

  List<Proveedor> getProveedores(Empresa empresa);

  Proveedor guardar(@Valid Proveedor proveedor);
  
}
