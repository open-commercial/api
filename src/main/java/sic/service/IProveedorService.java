package sic.service;

import java.util.List;

import org.springframework.data.domain.Page;
import sic.modelo.BusquedaProveedorCriteria;
import sic.modelo.Sucursal;
import sic.modelo.Proveedor;

import javax.validation.Valid;

public interface IProveedorService {

  Proveedor getProveedorNoEliminadoPorId(long id_Proveedor);

  void actualizar(@Valid Proveedor proveedor);

  Page<Proveedor> buscarProveedores(BusquedaProveedorCriteria criteria);

  void eliminar(long idProveedor);

  Proveedor getProveedorPorIdFiscal(Long idFiscal, Sucursal sucursal);

  Proveedor getProveedorPorRazonSocial(String razonSocial, Sucursal sucursal);

  List<Proveedor> getProveedores(Sucursal sucursal);

  Proveedor guardar(@Valid Proveedor proveedor);

  String generarNroDeProveedor(Sucursal sucursal);
}
