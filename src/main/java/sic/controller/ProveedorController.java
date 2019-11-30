package sic.controller;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sic.aspect.AccesoRolesPermitidos;
import sic.modelo.*;
import sic.modelo.criteria.BusquedaProveedorCriteria;
import sic.modelo.dto.ProveedorDTO;
import sic.service.IProveedorService;
import sic.service.IUbicacionService;

@RestController
@RequestMapping("/api/v1")
public class ProveedorController {

  private final IProveedorService proveedorService;
  private final IUbicacionService ubicacionService;
  private final ModelMapper modelMapper;

  @Autowired
  public ProveedorController(
    IProveedorService proveedorService,
    IUbicacionService ubicacionService,
    ModelMapper modelMapper) {
    this.proveedorService = proveedorService;
    this.ubicacionService = ubicacionService;
    this.modelMapper = modelMapper;
  }

  @GetMapping("/proveedores/{idProveedor}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public Proveedor getProveedorPorId(@PathVariable long idProveedor) {
    return this.proveedorService.getProveedorNoEliminadoPorId(idProveedor);
  }

  @PostMapping("/proveedores")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public Proveedor guardar(@RequestBody ProveedorDTO proveedorDTO) {
    Proveedor proveedor = modelMapper.map(proveedorDTO, Proveedor.class);
    proveedor.setUbicacion(null);
    if (proveedorDTO.getUbicacion() != null) {
      proveedor.setUbicacion(modelMapper.map(proveedorDTO.getUbicacion(), Ubicacion.class));
    }
    return proveedorService.guardar(proveedor);
  }

  @PutMapping("/proveedores")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public void actualizar(@RequestBody ProveedorDTO proveedorDTO) {
    Proveedor proveedorPersistido =
        proveedorService.getProveedorNoEliminadoPorId(proveedorDTO.getIdProveedor());
    Proveedor proveedorPorActualizar = modelMapper.map(proveedorDTO, Proveedor.class);
    proveedorPorActualizar.setNroProveedor(proveedorPersistido.getNroProveedor());
    if (proveedorPorActualizar.getRazonSocial() == null
        || proveedorPorActualizar.getRazonSocial().isEmpty()) {
      proveedorPorActualizar.setRazonSocial(proveedorPersistido.getRazonSocial());
    }
    if (proveedorPorActualizar.getCategoriaIVA() == null) {
      proveedorPorActualizar.setCategoriaIVA(proveedorPersistido.getCategoriaIVA());
    }
    if (proveedorPersistido.getUbicacion() != null) {
      proveedorPorActualizar.setUbicacion(proveedorPersistido.getUbicacion());
    } else {
      proveedorPorActualizar.setUbicacion(null);
    }
    if (proveedorDTO.getUbicacion() != null) {
      Ubicacion ubicacion = modelMapper.map(proveedorDTO.getUbicacion(), Ubicacion.class);
      ubicacion.setLocalidad(ubicacionService.getLocalidadPorId(ubicacion.getIdLocalidad()));
      proveedorPorActualizar.setUbicacion(ubicacion);
    } else {
      proveedorPorActualizar.setUbicacion(proveedorPersistido.getUbicacion());
    }
    if (proveedorService.getProveedorNoEliminadoPorId(proveedorPorActualizar.getIdProveedor())
        != null) {
      proveedorService.actualizar(proveedorPorActualizar);
    }
  }

  @PostMapping("/proveedores/busqueda/criteria")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public Page<Proveedor> buscarProveedores(
    @RequestBody BusquedaProveedorCriteria criteria) {
    return proveedorService.buscarProveedores(criteria);
  }

  @DeleteMapping("/proveedores/{idProveedor}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public void eliminar(@PathVariable long idProveedor) {
    proveedorService.eliminar(idProveedor);
  }

  @GetMapping("/proveedores")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public List<Proveedor> getProveedores() {
    return proveedorService.getProveedores();
  }
}
