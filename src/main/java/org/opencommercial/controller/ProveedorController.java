package org.opencommercial.controller;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.opencommercial.aspect.AccesoRolesPermitidos;
import org.opencommercial.model.Proveedor;
import org.opencommercial.model.Rol;
import org.opencommercial.model.Ubicacion;
import org.opencommercial.model.criteria.BusquedaProveedorCriteria;
import org.opencommercial.model.dto.ProveedorDTO;
import org.opencommercial.service.ProveedorService;
import org.opencommercial.service.UbicacionService;

import java.util.List;

@RestController
public class ProveedorController {

  private final ProveedorService proveedorService;
  private final UbicacionService ubicacionService;
  private final ModelMapper modelMapper;

  @Autowired
  public ProveedorController(ProveedorService proveedorService,
                             UbicacionService ubicacionService,
                             ModelMapper modelMapper) {
    this.proveedorService = proveedorService;
    this.ubicacionService = ubicacionService;
    this.modelMapper = modelMapper;
  }

  @GetMapping("/api/v1/proveedores/{idProveedor}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public Proveedor getProveedorPorId(@PathVariable long idProveedor) {
    return this.proveedorService.getProveedorNoEliminadoPorId(idProveedor);
  }

  @PostMapping("/api/v1/proveedores")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public Proveedor guardar(@RequestBody ProveedorDTO proveedorDTO) {
    Proveedor proveedor = modelMapper.map(proveedorDTO, Proveedor.class);
    proveedor.setUbicacion(null);
    if (proveedorDTO.getUbicacion() != null) {
      proveedor.setUbicacion(modelMapper.map(proveedorDTO.getUbicacion(), Ubicacion.class));
    }
    return proveedorService.guardar(proveedor);
  }

  @PutMapping("/api/v1/proveedores")
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

  @PostMapping("/api/v1/proveedores/busqueda/criteria")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public Page<Proveedor> buscarProveedores(@RequestBody BusquedaProveedorCriteria criteria) {
    return proveedorService.buscarProveedores(criteria);
  }

  @DeleteMapping("/api/v1/proveedores/{idProveedor}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public void eliminar(@PathVariable long idProveedor) {
    proveedorService.eliminar(idProveedor);
  }

  @GetMapping("/api/v1/proveedores")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public List<Proveedor> getProveedores() {
    return proveedorService.getProveedores();
  }
}
