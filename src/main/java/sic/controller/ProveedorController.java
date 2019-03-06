package sic.controller;

import java.util.List;
import java.util.ResourceBundle;

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
import sic.service.IEmpresaService;
import sic.service.IProveedorService;

@RestController
@RequestMapping("/api/v1")
public class ProveedorController {

  private final IProveedorService proveedorService;
  private final IEmpresaService empresaService;
  private final ModelMapper modelMapper;

  @Autowired
  public ProveedorController(
    IProveedorService proveedorService,
    IEmpresaService empresaService,
    ModelMapper modelMapper) {
    this.proveedorService = proveedorService;
    this.empresaService = empresaService;
    this.modelMapper = modelMapper;
  }

  @GetMapping("/proveedores/{idProveedor}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public Proveedor getProveedorPorId(@PathVariable long idProveedor) {
    return this.proveedorService.getProveedorPorId(idProveedor);
  }

  @PostMapping("/proveedores")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public Proveedor guardar(
    @RequestBody ProveedorDTO proveedorDTO,
    @RequestParam Long idEmpresa) {
    Proveedor proveedor = modelMapper.map(proveedorDTO, Proveedor.class);
    proveedor.setEmpresa(empresaService.getEmpresaPorId(idEmpresa));
    return proveedorService.guardar(proveedor);
  }

  @PutMapping("/proveedores")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public void actualizar(
    @RequestBody ProveedorDTO proveedorDTO,
    @RequestParam(required = false) Long idEmpresa) {
    Proveedor proveedorPersistido =
      proveedorService.getProveedorPorId(proveedorDTO.getId_Proveedor());
    Proveedor proveedorPorActualizar = modelMapper.map(proveedorDTO, Proveedor.class);
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
    if (idEmpresa != null) {
      proveedorPorActualizar.setEmpresa(empresaService.getEmpresaPorId(idEmpresa));
    }
    if (proveedorService.getProveedorPorId(proveedorPorActualizar.getId_Proveedor()) != null) {
      proveedorService.actualizar(proveedorPorActualizar);
    }
  }

  @GetMapping("/proveedores/busqueda/criteria")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public Page<Proveedor> buscarProveedores(
    @RequestParam(value = "idEmpresa") long idEmpresa,
    @RequestParam(required = false) String codigo,
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
      pageable = new PageRequest(pagina, tamanio, new Sort(Sort.Direction.ASC, "razonSocial"));
    } else {
      switch (sentido) {
        case "ASC":
          pageable = new PageRequest(pagina, tamanio, new Sort(Sort.Direction.ASC, ordenarPor));
          break;
        case "DESC":
          pageable = new PageRequest(pagina, tamanio, new Sort(Sort.Direction.DESC, ordenarPor));
          break;
        default:
          pageable = new PageRequest(pagina, tamanio, new Sort(Sort.Direction.ASC, "razonSocial"));
          break;
      }
    }
    BusquedaProveedorCriteria criteria =
      BusquedaProveedorCriteria.builder()
        .buscaPorCodigo(codigo != null)
        .codigo(codigo)
        .buscaPorRazonSocial(razonSocial != null)
        .razonSocial(razonSocial)
        .buscaPorIdFiscal(idFiscal != null)
        .idFiscal(idFiscal)
        .buscaPorProvincia(idProvincia != null)
        .idProvincia(idProvincia)
        .buscaPorLocalidad(idLocalidad != null)
        .idLocalidad(idLocalidad)
        .idEmpresa(idEmpresa)
        .pageable(pageable)
        .build();
    return proveedorService.buscarProveedores(criteria);
  }

  @DeleteMapping("/proveedores/{idProveedor}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public void eliminar(@PathVariable long idProveedor) {
    proveedorService.eliminar(idProveedor);
  }

  @GetMapping("/proveedores/empresas/{idEmpresa}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public List<Proveedor> getProveedores(@PathVariable long idEmpresa) {
    return proveedorService.getProveedores(empresaService.getEmpresaPorId(idEmpresa));
  }
}
