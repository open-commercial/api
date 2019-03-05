package sic.controller;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import sic.aspect.AccesoRolesPermitidos;
import sic.modelo.*;
import sic.modelo.dto.UbicacionDTO;
import sic.service.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class UbicacionController {

  private final IUbicacionService ubicacionService;
  private final IClienteService clienteService;
  private final IEmpresaService empresaService;
  private final IProveedorService proveedorService;
  private final ITransportistaService transportistaService;
  private final ModelMapper modelMapper;

  @Autowired
  public UbicacionController(
      IUbicacionService ubicacionService,
      IClienteService clienteService,
      IEmpresaService empresaService,
      IProveedorService proveedorService,
      ITransportistaService transportistaService,
      ModelMapper modelMapper) {
    this.ubicacionService = ubicacionService;
    this.clienteService = clienteService;
    this.empresaService = empresaService;
    this.proveedorService = proveedorService;
    this.transportistaService = transportistaService;
    this.modelMapper = modelMapper;
  }

  @GetMapping("/ubicaciones/localidades/{idLocalidad}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
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
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
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

  @PostMapping("/ubicaciones/clientes/{idCliente}/facturacion")
  @AccesoRolesPermitidos({
    Rol.ADMINISTRADOR,
    Rol.ENCARGADO,
    Rol.VENDEDOR,
    Rol.VIAJANTE,
    Rol.COMPRADOR
  })
  public Ubicacion guardarUbicacionDeFacturacion(
      @RequestBody UbicacionDTO ubicacionDTO, @PathVariable Long idCliente) {
    Cliente cliente = clienteService.getClientePorId(idCliente);
    Ubicacion ubicacion = modelMapper.map(ubicacionDTO, Ubicacion.class);
    return ubicacionService.guardarUbicacionDeFacturacionCliente(
        ubicacion,
        ubicacionDTO.getNombreLocalidad(),
        ubicacionDTO.getCodigoPostal(),
        ubicacionDTO.getNombreProvincia(),
        cliente);
  }

  @PostMapping("/ubicaciones/clientes/{idCliente}/envio")
  @AccesoRolesPermitidos({
    Rol.ADMINISTRADOR,
    Rol.ENCARGADO,
    Rol.VENDEDOR,
    Rol.VIAJANTE,
    Rol.COMPRADOR
  })
  public Ubicacion guardarUbicacionDeEnvio(
      @RequestBody UbicacionDTO ubicacionDTO, @PathVariable Long idCliente) {
    Cliente cliente = clienteService.getClientePorId(idCliente);
    Ubicacion ubicacion = modelMapper.map(ubicacionDTO, Ubicacion.class);
    return ubicacionService.guardarUbicacionDeEnvioCliente(
        ubicacion,
        ubicacionDTO.getNombreLocalidad(),
        ubicacionDTO.getCodigoPostal(),
        ubicacionDTO.getNombreProvincia(),
        cliente);
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
      ubicacionService.actualizar(ubicacion, ubicacionDTO.getNombreLocalidad(), ubicacionDTO.getCodigoPostal(), ubicacionDTO.getNombreProvincia());
    }
  }

  @PostMapping("/ubicaciones/empresas/{idEmpresa}")
  @AccesoRolesPermitidos(Rol.ADMINISTRADOR)
  public Ubicacion guardarUbicacionDeEmpresa(
      @RequestBody UbicacionDTO ubicacionDTO, @PathVariable Long idEmpresa) {
    Empresa empresa = empresaService.getEmpresaPorId(idEmpresa);
    Ubicacion ubicacion = modelMapper.map(ubicacionDTO, Ubicacion.class);
    return ubicacionService.guardaUbicacionEmpresa(
        ubicacion,
        ubicacionDTO.getNombreLocalidad(),
        ubicacionDTO.getCodigoPostal(),
        ubicacionDTO.getNombreProvincia(),
        empresa);
  }

  @PostMapping("/ubicaciones/proveedores/{idProveedor}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public Ubicacion guardarUbicacionDeProveedor(
      @RequestBody UbicacionDTO ubicacionDTO, @PathVariable Long idProveedor) {
    Proveedor proveedor = proveedorService.getProveedorPorId(idProveedor);
    Ubicacion ubicacion = modelMapper.map(ubicacionDTO, Ubicacion.class);
    return ubicacionService.guardaUbicacionProveedor(
        ubicacion,
        ubicacionDTO.getNombreLocalidad(),
        ubicacionDTO.getCodigoPostal(),
        ubicacionDTO.getNombreProvincia(),
        proveedor);
  }

  @PostMapping("/ubicaciones/transportistas/{idTransportista}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public Ubicacion guardarUbicacionDeTransportista(
      @RequestBody UbicacionDTO ubicacionDTO, @PathVariable Long idTransportista) {
    Ubicacion ubicacion = modelMapper.map(ubicacionDTO, Ubicacion.class);
    return ubicacionService.guardarUbicacionTransportista(
        ubicacion,
        ubicacionDTO.getNombreLocalidad(),
        ubicacionDTO.getCodigoPostal(),
        ubicacionDTO.getNombreProvincia(),
        transportistaService.getTransportistaPorId(idTransportista));
  }
}
