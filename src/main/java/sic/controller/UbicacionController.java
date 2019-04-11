package sic.controller;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import sic.aspect.AccesoRolesPermitidos;
import sic.modelo.*;
import sic.modelo.dto.LocalidadDTO;
import sic.modelo.dto.UbicacionDTO;
import sic.service.*;

import java.util.List;
import java.util.ResourceBundle;

@RestController
@RequestMapping("/api/v1")
public class UbicacionController {

  private final IUbicacionService ubicacionService;
  private final IEmpresaService empresaService;
  private final IProveedorService proveedorService;
  private final IClienteService clienteService;
  private final ITransportistaService transportistaService;
  private static final int TAMANIO_PAGINA_DEFAULT = 25;
  private final ModelMapper modelMapper;

  @Autowired
  public UbicacionController(
    IUbicacionService ubicacionService,
    IEmpresaService empresaService,
    IProveedorService proveedorService,
    IClienteService clienteService,
    ITransportistaService transportistaService,
    ModelMapper modelMapper) {
    this.ubicacionService = ubicacionService;
    this.empresaService = empresaService;
    this.proveedorService = proveedorService;
    this.clienteService = clienteService;
    this.transportistaService = transportistaService;
    this.modelMapper = modelMapper;
  }

  @GetMapping("/ubicaciones/{idUbicacion}")
  @AccesoRolesPermitidos({
    Rol.ADMINISTRADOR,
    Rol.ENCARGADO,
    Rol.VENDEDOR,
    Rol.VIAJANTE,
    Rol.COMPRADOR
  })
  public Ubicacion getUbicacionPorId(@PathVariable Long idUbicacion) {
    return ubicacionService.getUbicacionPorId(idUbicacion);
  }

  @GetMapping("/ubicaciones/localidades/{idLocalidad}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE})
  public Localidad getLocalidadPorId(@PathVariable long idLocalidad) {
    return ubicacionService.getLocalidadPorId(idLocalidad);
  }

  @GetMapping("/ubicaciones/localidades/provincias/{idProvincia}")
  @AccesoRolesPermitidos({
    Rol.ADMINISTRADOR,
    Rol.ENCARGADO,
    Rol.VENDEDOR,
    Rol.VIAJANTE,
    Rol.COMPRADOR
  })
  public List<Localidad> getLocalidadesDeLaProvincia(@PathVariable long idProvincia) {
    return ubicacionService.getLocalidadesDeLaProvincia(
      ubicacionService.getProvinciaPorId(idProvincia));
  }

  @GetMapping("/ubicaciones/provincias/{idProvincia}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE})
  public Provincia getProvinciaPorId(@PathVariable long idProvincia) {
    return ubicacionService.getProvinciaPorId(idProvincia);
  }

  @GetMapping("/ubicaciones/provincias")
  @AccesoRolesPermitidos({
    Rol.ADMINISTRADOR,
    Rol.ENCARGADO,
    Rol.VENDEDOR,
    Rol.COMPRADOR,
    Rol.VIAJANTE
  })
  public List<Provincia> getProvincias() {
    return ubicacionService.getProvincias();
  }

  @PutMapping("/ubicaciones")
  @AccesoRolesPermitidos({
    Rol.ADMINISTRADOR,
    Rol.ENCARGADO,
    Rol.VENDEDOR,
    Rol.VIAJANTE,
    Rol.COMPRADOR
  })
  public void actualizar(@RequestBody UbicacionDTO ubicacionDTO) {
    if (ubicacionDTO.getIdUbicacion() != 0L) {
      Ubicacion ubicacion = modelMapper.map(ubicacionDTO, Ubicacion.class);
      ubicacion.setLocalidad(ubicacionService.getLocalidadPorId(ubicacionDTO.getIdLocalidad()));
      ubicacionService.actualizar(ubicacion);
    }
  }

  @PostMapping("/ubicaciones/empresas/{idEmpresa}")
  @AccesoRolesPermitidos(Rol.ADMINISTRADOR)
  public Ubicacion guardarUbicacionDeEmpresa(
    @RequestBody UbicacionDTO ubicacionDTO, @PathVariable Long idEmpresa) {
    Empresa empresa = empresaService.getEmpresaPorId(idEmpresa);
    Ubicacion ubicacion = modelMapper.map(ubicacionDTO, Ubicacion.class);
    ubicacion.setLocalidad(ubicacionService.getLocalidadPorId(ubicacionDTO.getIdLocalidad()));
    return ubicacionService.guardaUbicacionEmpresa(
      ubicacion,
      empresa);
  }

  @PostMapping("/ubicaciones/proveedores/{idProveedor}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public Ubicacion guardarUbicacionDeProveedor(
    @RequestBody UbicacionDTO ubicacionDTO, @PathVariable Long idProveedor) {
    Proveedor proveedor = proveedorService.getProveedorPorId(idProveedor);
    Ubicacion ubicacion = modelMapper.map(ubicacionDTO, Ubicacion.class);
    ubicacion.setLocalidad(ubicacionService.getLocalidadPorId(ubicacionDTO.getIdLocalidad()));
    return ubicacionService.guardaUbicacionProveedor(
      ubicacion,
      proveedor);
  }

  @PostMapping("/ubicaciones/transportistas/{idTransportista}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public Ubicacion guardarUbicacionDeTransportista(
    @RequestBody UbicacionDTO ubicacionDTO, @PathVariable Long idTransportista) {
    Ubicacion ubicacion = modelMapper.map(ubicacionDTO, Ubicacion.class);
    ubicacion.setLocalidad(ubicacionService.getLocalidadPorId(ubicacionDTO.getIdLocalidad()));
    return ubicacionService.guardarUbicacionTransportista(
      ubicacion,
      transportistaService.getTransportistaPorId(idTransportista));
  }

  @PutMapping("/ubicaciones/localidades")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public void actualizar(@RequestBody LocalidadDTO localidadDTO) {
    Localidad localidadPersistida =
      ubicacionService.getLocalidadPorId(localidadDTO.getIdLocalidad());
    Localidad localidadPorActualizar = modelMapper.map(localidadDTO, Localidad.class);
    if (localidadPorActualizar.getNombre() != null
      && !localidadPorActualizar.getNombre().equals(localidadPersistida.getNombre())) {
      throw new BusinessServiceException(
        ResourceBundle.getBundle("Mensajes").getString("mensaje_localidad_cambio_nombre"));
    }
    if (localidadPorActualizar.getCodigoPostal() == null) {
      localidadPorActualizar.setCodigoPostal(localidadPersistida.getCodigoPostal());
    }
    localidadPorActualizar.setNombre(localidadPersistida.getNombre());
    localidadPorActualizar.setProvincia(localidadPersistida.getProvincia());
    if (ubicacionService.getLocalidadPorId(localidadPorActualizar.getIdLocalidad()) != null) {
      ubicacionService.actualizarLocalidad(localidadPorActualizar);
    }
  }

  @GetMapping("/ubicaciones/localidades/busqueda/criteria")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public Page<Localidad> buscarConCriteria(
    @RequestParam(required = false) String nombreLocalidad,
    @RequestParam(required = false) String codigoPostal,
    @RequestParam(required = false) String nombreProvincia,
    @RequestParam(required = false) Boolean envioGratuito,
    @RequestParam(required = false) Integer pagina,
    @RequestParam(required = false) String ordenarPor,
    @RequestParam(required = false) String sentido) {
    if (pagina == null || pagina < 0) pagina = 0;
    Pageable pageable;
    if (ordenarPor == null || sentido == null) {
      pageable =
        new PageRequest(
          pagina, TAMANIO_PAGINA_DEFAULT, new Sort(Sort.Direction.ASC, "nombre"));
    } else {
      switch (sentido) {
        case "ASC":
          pageable =
            new PageRequest(
              pagina, TAMANIO_PAGINA_DEFAULT, new Sort(Sort.Direction.ASC, ordenarPor));
          break;
        case "DESC":
          pageable =
            new PageRequest(
              pagina, TAMANIO_PAGINA_DEFAULT, new Sort(Sort.Direction.DESC, ordenarPor));
          break;
        default:
          pageable =
            new PageRequest(
              pagina, TAMANIO_PAGINA_DEFAULT, new Sort(Sort.Direction.ASC, "nombre"));
          break;
      }
    }
    BusquedaLocalidadCriteria criteria = BusquedaLocalidadCriteria.builder()
      .buscaPorNombre(nombreLocalidad != null)
      .nombre(nombreLocalidad)
      .buscaPorCodigoPostal(codigoPostal != null)
      .codigoPostal(codigoPostal)
      .buscaPorNombreProvincia(nombreProvincia != null)
      .nombreProvincia(nombreProvincia)
      .buscaPorEnvio(envioGratuito != null)
      .envioGratuito(envioGratuito)
      .pageable(pageable)
      .build();
    return ubicacionService.buscar(criteria);
  }
}
