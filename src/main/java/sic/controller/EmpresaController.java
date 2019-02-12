package sic.controller;

import java.util.List;
import java.util.ResourceBundle;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import sic.aspect.AccesoRolesPermitidos;
import sic.modelo.Empresa;
import sic.modelo.Rol;
import sic.modelo.dto.EmpresaDTO;
import sic.service.BusinessServiceException;
import sic.service.IEmpresaService;
import sic.service.ILocalidadService;

@RestController
@RequestMapping("/api/v1")
public class EmpresaController {

  public final IEmpresaService empresaService;
  private final ILocalidadService localidadService;
  private final ModelMapper modelMapper;

  @Autowired
  public EmpresaController(
      IEmpresaService empresaService, ILocalidadService localidadService, ModelMapper modelMapper) {
    this.empresaService = empresaService;
    this.localidadService = localidadService;
    this.modelMapper = modelMapper;
  }

  @GetMapping("/empresas")
  @AccesoRolesPermitidos({
    Rol.ADMINISTRADOR,
    Rol.ENCARGADO,
    Rol.VENDEDOR,
    Rol.VIAJANTE,
    Rol.COMPRADOR
  })
  public List<Empresa> getEmpresas() {
    return empresaService.getEmpresas();
  }

  @GetMapping("/empresas/{idEmpresa}")
  @AccesoRolesPermitidos({
    Rol.ADMINISTRADOR,
    Rol.ENCARGADO,
    Rol.VENDEDOR,
    Rol.VIAJANTE,
    Rol.COMPRADOR
  })
  public Empresa getEmpresaPorId(@PathVariable long idEmpresa) {
    return empresaService.getEmpresaPorId(idEmpresa);
  }

  @PostMapping("/empresas")
  @AccesoRolesPermitidos(Rol.ADMINISTRADOR)
  public Empresa guardar(@RequestBody EmpresaDTO empresaDTO) {
    Empresa empresa = modelMapper.map(empresaDTO, Empresa.class);
    if (empresaDTO.getUbicacion() != null && empresaDTO.getUbicacion().getIdLocalidad() != 0) {
      empresa
          .getUbicacion()
          .setLocalidad(
              localidadService.getLocalidadPorId(empresaDTO.getUbicacion().getIdLocalidad()));
    }
    return empresaService.guardar(empresa);
  }

  @PutMapping("/empresas")
  @AccesoRolesPermitidos(Rol.ADMINISTRADOR)
  public void actualizar(@RequestBody EmpresaDTO empresaDTO) {
    Empresa empresaParaActualizar = modelMapper.map(empresaDTO, Empresa.class);
    Empresa empresaPersistida = empresaService.getEmpresaPorId(empresaParaActualizar.getId_Empresa());
    if (empresaParaActualizar.getNombre() == null || empresaParaActualizar.getNombre().isEmpty()) {
      empresaParaActualizar.setNombre(empresaPersistida.getNombre());
    }
    if (empresaParaActualizar.getCategoriaIVA() == null) {
      empresaParaActualizar.setCategoriaIVA(empresaPersistida.getCategoriaIVA());
    }
    if (empresaDTO.getUbicacion() != null) {
      if (empresaDTO.getUbicacion().getIdUbicacion()
          == empresaPersistida.getUbicacion().getIdUbicacion()) {
        empresaParaActualizar.setUbicacion(empresaPersistida.getUbicacion());
      } else {
        throw new BusinessServiceException(
            ResourceBundle.getBundle("Mensajes").getString("mensaje_error_ubicacion_incorrecta"));
      }
      if (empresaDTO.getUbicacion().getIdLocalidad()
          != empresaPersistida.getUbicacion().getLocalidad().getId_Localidad()) {
        empresaParaActualizar
            .getUbicacion()
            .setLocalidad(
                localidadService.getLocalidadPorId(empresaDTO.getUbicacion().getIdLocalidad()));
      }
    }
    empresaService.actualizar(empresaParaActualizar, empresaPersistida);
  }

  @DeleteMapping("/empresas/{idEmpresa}")
  @AccesoRolesPermitidos(Rol.ADMINISTRADOR)
  public void eliminar(@PathVariable long idEmpresa) {
    empresaService.eliminar(idEmpresa);
  }

  @PostMapping("/empresas/{idEmpresa}/logo")
  @AccesoRolesPermitidos(Rol.ADMINISTRADOR)
  public String uploadLogo(@PathVariable long idEmpresa, @RequestBody byte[] imagen) {
    return empresaService.guardarLogo(idEmpresa, imagen);
  }
}
