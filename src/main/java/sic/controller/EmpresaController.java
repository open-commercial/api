package sic.controller;

import java.util.List;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import sic.aspect.AccesoRolesPermitidos;
import sic.modelo.Empresa;
import sic.modelo.Rol;
import sic.modelo.Ubicacion;
import sic.modelo.dto.EmpresaDTO;
import sic.service.IEmpresaService;
import sic.service.IUbicacionService;

@RestController
@RequestMapping("/api/v1")
public class EmpresaController {

  public final IEmpresaService empresaService;
  public final IUbicacionService ubicacionService;
  private final ModelMapper modelMapper;

  @Autowired
  public EmpresaController(
      IEmpresaService empresaService, IUbicacionService ubicacionService, ModelMapper modelMapper) {
    this.empresaService = empresaService;
    this.ubicacionService = ubicacionService;
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
    empresa.setUbicacion(null);
    if (empresaDTO.getUbicacion() != null) {
      empresa.setUbicacion(modelMapper.map(empresaDTO.getUbicacion(), Ubicacion.class));
    }
    return empresaService.guardar(empresa);
  }

  @PutMapping("/empresas")
  @AccesoRolesPermitidos(Rol.ADMINISTRADOR)
  public void actualizar(@RequestBody EmpresaDTO empresaDTO) {
    Empresa empresaParaActualizar = modelMapper.map(empresaDTO, Empresa.class);
    Empresa empresaPersistida =
        empresaService.getEmpresaPorId(empresaParaActualizar.getIdEmpresa());
    if (empresaParaActualizar.getNombre() == null || empresaParaActualizar.getNombre().isEmpty()) {
      empresaParaActualizar.setNombre(empresaPersistida.getNombre());
    }
    if (empresaParaActualizar.getCategoriaIVA() == null) {
      empresaParaActualizar.setCategoriaIVA(empresaPersistida.getCategoriaIVA());
    }
    Ubicacion ubicacion;
    if (empresaDTO.getUbicacion() != null) {
      ubicacion = modelMapper.map(empresaDTO.getUbicacion(), Ubicacion.class);
      ubicacion.setLocalidad(
          ubicacionService.getLocalidadPorId(
              modelMapper.map(empresaDTO.getUbicacion(), Ubicacion.class).getIdLocalidad()));
      empresaParaActualizar.setUbicacion(ubicacion);
    } else {
      empresaParaActualizar.setUbicacion(empresaPersistida.getUbicacion());
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
