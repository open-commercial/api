package sic.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import sic.aspect.AccesoRolesPermitidos;
import sic.modelo.*;
import sic.service.IEmpresaService;
import sic.service.IProveedorService;

@RestController
@RequestMapping("/api/v1")
public class ProveedorController {

  private final IProveedorService proveedorService;
  private final IEmpresaService empresaService;

  @Autowired
  public ProveedorController(IProveedorService proveedorService, IEmpresaService empresaService) {
    this.proveedorService = proveedorService;
    this.empresaService = empresaService;
  }

  @GetMapping("/proveedores/{idProveedor}")
  @ResponseStatus(HttpStatus.OK)
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public Proveedor getProveedorPorId(@PathVariable long idProveedor) {
    return this.proveedorService.getProveedorPorId(idProveedor);
  }

  @PostMapping("/proveedores")
  @ResponseStatus(HttpStatus.CREATED)
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public Proveedor guardar(@RequestBody Proveedor proveedor) {
    return proveedorService.guardar(proveedor);
  }

  @PutMapping("/proveedores")
  @ResponseStatus(HttpStatus.OK)
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public void actualizar(@RequestBody Proveedor proveedor) {
    if (proveedorService.getProveedorPorId(proveedor.getId_Proveedor()) != null) {
      proveedorService.actualizar(proveedor);
    }
  }

  @GetMapping("/proveedores/busqueda/criteria")
  @ResponseStatus(HttpStatus.OK)
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public List<Proveedor> buscarProveedores(
      @RequestParam(value = "idEmpresa") long idEmpresa,
      @RequestParam(required = false) String codigo,
      @RequestParam(required = false) String razonSocial,
      @RequestParam(required = false) String idFiscal,
      @RequestParam(required = false) Long idPais,
      @RequestParam(required = false) Long idProvincia,
      @RequestParam(required = false) Long idLocalidad) {
    BusquedaProveedorCriteria criteria =
        new BusquedaProveedorCriteria(
            (codigo != null),
            codigo,
            (razonSocial != null),
            razonSocial,
            (idFiscal != null),
            idFiscal,
            (idPais != null),
            idPais,
            (idProvincia != null),
            idProvincia,
            (idLocalidad != null),
            idLocalidad,
            idEmpresa,
            0);
    return proveedorService.buscarProveedores(criteria);
  }

  @DeleteMapping("/proveedores/{idProveedor}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public void eliminar(@PathVariable long idProveedor) {
    proveedorService.eliminar(idProveedor);
  }

  @GetMapping("/proveedores/empresas/{idEmpresa}")
  @ResponseStatus(HttpStatus.OK)
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public List<Proveedor> getProveedores(@PathVariable long idEmpresa) {
    return proveedorService.getProveedores(empresaService.getEmpresaPorId(idEmpresa));
  }
}
