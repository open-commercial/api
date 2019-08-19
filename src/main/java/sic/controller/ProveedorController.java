package sic.controller;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sic.aspect.AccesoRolesPermitidos;
import sic.modelo.*;
import sic.modelo.dto.ProveedorDTO;
import sic.service.ISucursalService;
import sic.service.IProveedorService;
import sic.service.IUbicacionService;

@RestController
@RequestMapping("/api/v1")
public class ProveedorController {

  private final IProveedorService proveedorService;
  private final ISucursalService sucursalService;
  private final IUbicacionService ubicacionService;
  private final ModelMapper modelMapper;

  @Autowired
  public ProveedorController(
    IProveedorService proveedorService,
    ISucursalService sucursalService,
    IUbicacionService ubicacionService,
    ModelMapper modelMapper) {
    this.proveedorService = proveedorService;
    this.sucursalService = sucursalService;
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
    proveedor.setSucursal(sucursalService.getSucursalPorId(proveedorDTO.getIdSucursal()));
    proveedor.setUbicacion(null);
    if (proveedorDTO.getUbicacion() != null) {
      proveedor.setUbicacion(modelMapper.map(proveedorDTO.getUbicacion(), Ubicacion.class));
    }
    return proveedorService.guardar(proveedor);
  }

  @PutMapping("/proveedores")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public void actualizar(
    @RequestBody ProveedorDTO proveedorDTO) {
    Proveedor proveedorPersistido =
      proveedorService.getProveedorNoEliminadoPorId(proveedorDTO.getId_Proveedor());
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
    if (proveedorDTO.getIdSucursal() != null) {
      proveedorPorActualizar.setSucursal(
          sucursalService.getSucursalPorId(proveedorDTO.getIdSucursal()));
    } else {
      proveedorPorActualizar.setSucursal(proveedorPersistido.getSucursal());
    }
    Ubicacion ubicacion;
    if (proveedorDTO.getUbicacion() != null) {
      ubicacion = modelMapper.map(proveedorDTO.getUbicacion(), Ubicacion.class);
      ubicacion.setLocalidad(ubicacionService.getLocalidadPorId(ubicacion.getIdLocalidad()));
      proveedorPorActualizar.setUbicacion(ubicacion);
    } else {
      proveedorPorActualizar.setUbicacion(proveedorPersistido.getUbicacion());
    }
    if (proveedorService.getProveedorNoEliminadoPorId(proveedorPorActualizar.getId_Proveedor()) != null) {
      proveedorService.actualizar(proveedorPorActualizar);
    }
  }

  @GetMapping("/proveedores/busqueda/criteria")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public Page<Proveedor> buscarProveedores(
    @RequestParam(value = "idSucursal") long idSucursal,
    @RequestParam(required = false) String nroProveedor,
    @RequestParam(required = false) String razonSocial,
    @RequestParam(required = false) Long idFiscal,
    @RequestParam(required = false) Long idProvincia,
    @RequestParam(required = false) Long idLocalidad,
    @RequestParam(required = false) Integer pagina,
    @RequestParam(required = false) Integer tamanio,
    @RequestParam(required = false) String ordenarPor,
    @RequestParam(required = false) String sentido) {
    final int TAMANIO_PAGINA_DEFAULT = 50;
    if (tamanio == null || tamanio <= 0) tamanio = TAMANIO_PAGINA_DEFAULT;
    if (pagina == null || pagina < 0) pagina = 0;
    Pageable pageable;
    if (ordenarPor == null || sentido == null) {
      pageable = PageRequest.of(pagina, tamanio, new Sort(Sort.Direction.ASC, "razonSocial"));
    } else {
      switch (sentido) {
        case "ASC":
          pageable = PageRequest.of(pagina, tamanio, new Sort(Sort.Direction.ASC, ordenarPor));
          break;
        case "DESC":
          pageable = PageRequest.of(pagina, tamanio, new Sort(Sort.Direction.DESC, ordenarPor));
          break;
        default:
          pageable = PageRequest.of(pagina, tamanio, new Sort(Sort.Direction.ASC, "razonSocial"));
          break;
      }
    }
    BusquedaProveedorCriteria criteria =
      BusquedaProveedorCriteria.builder()
        .buscaPorNroProveedor(nroProveedor != null)
        .nroProveedor(nroProveedor)
        .buscaPorRazonSocial(razonSocial != null)
        .razonSocial(razonSocial)
        .buscaPorIdFiscal(idFiscal != null)
        .idFiscal(idFiscal)
        .buscaPorProvincia(idProvincia != null)
        .idProvincia(idProvincia)
        .buscaPorLocalidad(idLocalidad != null)
        .idLocalidad(idLocalidad)
        .idSucursal(idSucursal)
        .pageable(pageable)
        .build();
    return proveedorService.buscarProveedores(criteria);
  }

  @DeleteMapping("/proveedores/{idProveedor}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public void eliminar(@PathVariable long idProveedor) {
    proveedorService.eliminar(idProveedor);
  }

  @GetMapping("/proveedores/sucursales/{idSucursal}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public List<Proveedor> getProveedores(@PathVariable long idSucursal) {
    return proveedorService.getProveedores(sucursalService.getSucursalPorId(idSucursal));
  }
}
