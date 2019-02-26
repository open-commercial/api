package sic.controller;

import io.jsonwebtoken.Claims;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import sic.aspect.AccesoRolesPermitidos;
import sic.modelo.*;
import sic.modelo.dto.ClienteDTO;
import sic.service.*;

import java.util.ResourceBundle;

@RestController
@RequestMapping("/api/v1")
public class ClienteController {

  private final IClienteService clienteService;
  private final IEmpresaService empresaService;
  private final IUbicacionService ubicacionService;
  private final IUsuarioService usuarioService;
  private final IAuthService authService;
  private final ModelMapper modelMapper;
  private static final int TAMANIO_PAGINA_DEFAULT = 25;

  @Autowired
  public ClienteController(
      IClienteService clienteService,
      IEmpresaService empresaService,
      IUbicacionService ubicacionService,
      IUsuarioService usuarioService,
      IAuthService authService,
      ModelMapper modelMapper) {
    this.clienteService = clienteService;
    this.empresaService = empresaService;
    this.ubicacionService = ubicacionService;
    this.usuarioService = usuarioService;
    this.authService = authService;
    this.modelMapper = modelMapper;
  }

  @GetMapping("/clientes/{idCliente}")
  @AccesoRolesPermitidos({
    Rol.ADMINISTRADOR,
    Rol.ENCARGADO,
    Rol.VENDEDOR,
    Rol.VIAJANTE,
    Rol.COMPRADOR
  })
  public Cliente getCliente(@PathVariable long idCliente) {
    return clienteService.getClientePorId(idCliente);
  }

  @GetMapping("/clientes/busqueda/criteria")
  public Page<Cliente> buscarConCriteria(
      @RequestParam Long idEmpresa,
      @RequestParam(required = false) String nroCliente,
      @RequestParam(required = false) String nombreFiscal,
      @RequestParam(required = false) String nombreFantasia,
      @RequestParam(required = false) Long idFiscal,
      @RequestParam(required = false) Long idViajante,
      @RequestParam(required = false) Long idProvincia,
      @RequestParam(required = false) Long idLocalidad,
      @RequestParam(required = false) Integer pagina,
      @RequestParam(required = false) String ordenarPor,
      @RequestParam(required = false) String sentido,
      @RequestHeader("Authorization") String authorizationHeader) {
    if (pagina == null || pagina < 0) pagina = 0;
    Pageable pageable;
    if (ordenarPor == null || sentido == null) {
      pageable =
          new PageRequest(
              pagina, TAMANIO_PAGINA_DEFAULT, new Sort(Sort.Direction.ASC, "nombreFiscal"));
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
                  pagina, TAMANIO_PAGINA_DEFAULT, new Sort(Sort.Direction.ASC, "nombreFiscal"));
          break;
      }
    }
    BusquedaClienteCriteria criteria =
        BusquedaClienteCriteria.builder()
            .buscaPorNombreFiscal(nombreFiscal != null)
            .nombreFiscal(nombreFiscal)
            .buscaPorNombreFantasia(nombreFantasia != null)
            .nombreFantasia(nombreFantasia)
            .buscaPorIdFiscal(idFiscal != null)
            .idFiscal(idFiscal)
            .buscaPorViajante(idViajante != null)
            .idViajante(idViajante)
            .buscaPorProvincia(idProvincia != null)
            .idProvincia(idProvincia)
            .buscaPorLocalidad(idLocalidad != null)
            .idLocalidad(idLocalidad)
            .buscarPorNroDeCliente(nroCliente != null)
            .nroDeCliente(nroCliente)
            .idEmpresa(idEmpresa)
            .pageable(pageable)
            .build();
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    return clienteService.buscarClientes(criteria, (int) claims.get("idUsuario"));
  }

  @GetMapping("/clientes/predeterminado/empresas/{idEmpresa}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public Cliente getClientePredeterminado(@PathVariable long idEmpresa) {
    return clienteService.getClientePredeterminado(empresaService.getEmpresaPorId(idEmpresa));
  }

  @GetMapping("/clientes/existe-predeterminado/empresas/{idEmpresa}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public boolean existeClientePredeterminado(@PathVariable long idEmpresa) {
    return clienteService.existeClientePredeterminado(empresaService.getEmpresaPorId(idEmpresa));
  }

  @DeleteMapping("/clientes/{idCliente}")
  @AccesoRolesPermitidos(Rol.ADMINISTRADOR)
  public void eliminar(@PathVariable long idCliente) {
    clienteService.eliminar(idCliente);
  }

  @PostMapping("/clientes")
  @AccesoRolesPermitidos({
    Rol.ADMINISTRADOR,
    Rol.ENCARGADO,
    Rol.VENDEDOR,
    Rol.VIAJANTE,
    Rol.COMPRADOR
  })
  public Cliente guardar(
      @RequestBody ClienteDTO nuevoCliente,
      @RequestParam Long idEmpresa,
      @RequestParam(required = false) Long idViajante,
      @RequestParam Long idCredencial,
      @RequestHeader("Authorization") String authorizationHeader) {
    Cliente cliente = modelMapper.map(nuevoCliente, Cliente.class);
    if (idCredencial != null) {
      Claims claims = authService.getClaimsDelToken(authorizationHeader);
      long idUsuarioLoggedIn = (int) claims.get("idUsuario");
      Usuario usuarioLoggedIn = usuarioService.getUsuarioPorId(idUsuarioLoggedIn);
      if (idCredencial != idUsuarioLoggedIn
          && !usuarioLoggedIn.getRoles().contains(Rol.ADMINISTRADOR)) {
        throw new ForbiddenException(
            ResourceBundle.getBundle("Mensajes").getString("mensaje_usuario_rol_no_valido"));
      } else {
        Usuario usuarioCredencial = usuarioService.getUsuarioPorId(idCredencial);
        cliente.setCredencial(usuarioCredencial);
      }
    }
    cliente.setEmpresa(empresaService.getEmpresaPorId(idEmpresa));
    if (idViajante != null) {
      cliente.setViajante(usuarioService.getUsuarioPorId(idViajante));
    }
    if (nuevoCliente.getUbicacionFacturacion() != null && nuevoCliente.getUbicacionFacturacion().getIdLocalidad() != null) {
      cliente
          .getUbicacionFacturacion()
          .setLocalidad(
              ubicacionService.getLocalidadPorId(
                  nuevoCliente.getUbicacionFacturacion().getIdLocalidad()));
    }
    if (nuevoCliente.getUbicacionEnvio() != null && nuevoCliente.getUbicacionEnvio().getIdLocalidad() != null) {
      cliente
        .getUbicacionEnvio()
        .setLocalidad(
          ubicacionService.getLocalidadPorId(
            nuevoCliente.getUbicacionEnvio().getIdLocalidad()));
    }
    if (nuevoCliente.getUbicacionFacturacion() != null && nuevoCliente.getUbicacionFacturacion().getIdLocalidad() == null) {
      Provincia provincia = new Provincia();
      provincia.setNombre(nuevoCliente.getUbicacionFacturacion().getNombreProvincia());
      Localidad localidad = new Localidad();
      localidad.setProvincia(provincia);
      localidad.setCodigoPostal(nuevoCliente.getUbicacionFacturacion().getCodigoPostal());
      localidad.setNombre(nuevoCliente.getUbicacionFacturacion().getNombreLocalidad());
      cliente.getUbicacionFacturacion().setLocalidad(localidad);
    }
    if (nuevoCliente.getUbicacionEnvio() != null && nuevoCliente.getUbicacionEnvio().getIdLocalidad() == null) {
      Provincia provincia = new Provincia();
      provincia.setNombre(nuevoCliente.getUbicacionEnvio().getNombreProvincia());
      Localidad localidad = new Localidad();
      localidad.setProvincia(provincia);
      localidad.setCodigoPostal(nuevoCliente.getUbicacionEnvio().getCodigoPostal());
      localidad.setNombre(nuevoCliente.getUbicacionEnvio().getNombreLocalidad());
      cliente.getUbicacionEnvio().setLocalidad(localidad);
    }
    return clienteService.guardar(cliente);
  }

  @PutMapping("/clientes")
  @AccesoRolesPermitidos({
    Rol.ADMINISTRADOR,
    Rol.ENCARGADO,
    Rol.VENDEDOR,
    Rol.VIAJANTE,
    Rol.COMPRADOR
  })
  public void actualizar(
      @RequestBody ClienteDTO clienteDTO,
      @RequestParam(required = false) Long idEmpresa,
      @RequestParam(required = false) Long idViajante,
      @RequestParam(required = false) Long idCredencial,
      @RequestHeader("Authorization") String authorizationHeader) {
    Cliente clientePorActualizar = modelMapper.map(clienteDTO, Cliente.class);
    Cliente clientePersistido =
        clienteService.getClientePorId(clientePorActualizar.getId_Cliente());
    if (idCredencial != null) {
      Claims claims = authService.getClaimsDelToken(authorizationHeader);
      long idUsuarioLoggedIn = (int) claims.get("idUsuario");
      Usuario usuarioLoggedIn = usuarioService.getUsuarioPorId(idUsuarioLoggedIn);
      if (idCredencial != idUsuarioLoggedIn
          && clientePersistido.getCredencial() != null
          && clientePersistido.getCredencial().getId_Usuario() != idCredencial
          && !usuarioLoggedIn.getRoles().contains(Rol.ADMINISTRADOR)) {
        throw new ForbiddenException(
            ResourceBundle.getBundle("Mensajes").getString("mensaje_usuario_rol_no_valido"));
      } else {
        Usuario usuarioCredencial = usuarioService.getUsuarioPorId(idCredencial);
        clientePorActualizar.setCredencial(usuarioCredencial);
      }
    } else {
      clientePorActualizar.setCredencial(clientePersistido.getCredencial());
    }
    if (clientePorActualizar.getBonificacion() != null
        && clientePersistido.getBonificacion().compareTo(clientePorActualizar.getBonificacion())
            != 0) {
      Claims claims = authService.getClaimsDelToken(authorizationHeader);
      long idUsuarioLoggedIn = (int) claims.get("idUsuario");
      Usuario usuarioLoggedIn = usuarioService.getUsuarioPorId(idUsuarioLoggedIn);
      if (!usuarioLoggedIn.getRoles().contains(Rol.ADMINISTRADOR)
          && !usuarioLoggedIn.getRoles().contains(Rol.ENCARGADO)) {
        throw new ForbiddenException(
            ResourceBundle.getBundle("Mensajes").getString("mensaje_usuario_rol_no_valido"));
      }
    } else {
      clientePorActualizar.setBonificacion(clientePersistido.getBonificacion());
    }
    if (clienteDTO.getUbicacionFacturacion() != null) {
      if (clienteDTO.getUbicacionFacturacion().getIdUbicacion()
          == clientePersistido.getUbicacionFacturacion().getIdUbicacion()) {
        clientePorActualizar.setUbicacionFacturacion(clientePersistido.getUbicacionFacturacion());
      } else {
        throw new BusinessServiceException(
            ResourceBundle.getBundle("Mensajes").getString("mensaje_error_ubicacion_incorrecta"));
      }
      if (clienteDTO.getUbicacionFacturacion().getIdLocalidad()
          != clientePersistido.getUbicacionFacturacion().getLocalidad().getId_Localidad()) {
        clientePorActualizar
            .getUbicacionFacturacion()
            .setLocalidad(
                ubicacionService.getLocalidadPorId(
                    clienteDTO.getUbicacionFacturacion().getIdLocalidad()));
      }
      if (clienteDTO.getUbicacionFacturacion().getIdLocalidad() == null) {
        Provincia provincia = new Provincia();
        provincia.setNombre(clienteDTO.getUbicacionFacturacion().getNombreProvincia());
        Localidad localidad = new Localidad();
        localidad.setProvincia(provincia);
        localidad.setCodigoPostal(clienteDTO.getUbicacionFacturacion().getCodigoPostal());
        localidad.setNombre(clienteDTO.getUbicacionFacturacion().getNombreLocalidad());
        clientePorActualizar.getUbicacionFacturacion().setLocalidad(localidad);
      }
    }
    if (clienteDTO.getUbicacionEnvio() != null) {
      if (clientePersistido.getUbicacionEnvio() != null) {
        if (clienteDTO.getUbicacionEnvio().getIdUbicacion()
          == clientePersistido.getUbicacionEnvio().getIdUbicacion()) {
          clientePorActualizar.setUbicacionEnvio(clientePersistido.getUbicacionFacturacion());
        } else {
          throw new BusinessServiceException(
            ResourceBundle.getBundle("Mensajes").getString("mensaje_error_ubicacion_incorrecta"));
        }
        if (clientePersistido.getUbicacionEnvio().getLocalidad() != null && (clienteDTO.getUbicacionEnvio().getIdLocalidad()
          != clientePersistido.getUbicacionEnvio().getLocalidad().getId_Localidad())) {
          clientePorActualizar
            .getUbicacionEnvio()
            .setLocalidad(
              ubicacionService.getLocalidadPorId(
                clienteDTO.getUbicacionFacturacion().getIdLocalidad()));
        }
      }
      if (clienteDTO.getUbicacionEnvio().getIdLocalidad() == null) {
        Provincia provincia = new Provincia();
        provincia.setNombre(clienteDTO.getUbicacionEnvio().getNombreProvincia());
        Localidad localidad = new Localidad();
        localidad.setProvincia(provincia);
        localidad.setCodigoPostal(clienteDTO.getUbicacionEnvio().getCodigoPostal());
        localidad.setNombre(clienteDTO.getUbicacionEnvio().getNombreLocalidad());
        clientePorActualizar.getUbicacionEnvio().setLocalidad(localidad);
      }
    }

    // probar lo de model mapper
    //      if (clienteDTO.getUbicacion().getDescripcion() != null
    //        && !clienteDTO
    //        .getUbicacion()
    //        .getDescripcion()
    //        .equals(clientePersistido.getUbicacionFacturacion().getDescripcion())) {
    //        clientePorActualizar
    //          .getUbicacionFacturacion()
    //          .setDescripcion(clienteDTO.getUbicacion().getDescripcion());
    //      }
    //      if (clienteDTO.getUbicacion().getLatitud() != null
    //        && !clienteDTO
    //        .getUbicacion()
    //        .getLatitud()
    //        .equals(clientePersistido.getUbicacionFacturacion().getLatitud())) {
    //
    // clientePorActualizar.getUbicacionFacturacion().setLatitud(clienteDTO.getUbicacion().getLatitud());
    //      }
    //      if (clienteDTO.getUbicacion().getLongitud() != null
    //        && !clienteDTO
    //        .getUbicacion()
    //        .getLongitud()
    //        .equals(clientePersistido.getUbicacionFacturacion().getLongitud())) {
    //
    // clientePorActualizar.getUbicacionFacturacion().setLongitud(clienteDTO.getUbicacion().getLongitud());
    //      }
    //      if (clienteDTO.getUbicacion().getCalle() != null
    //        && !clienteDTO
    //        .getUbicacion()
    //        .getCalle()
    //        .equals(clientePersistido.getUbicacionFacturacion().getCalle())) {
    //
    // clientePorActualizar.getUbicacionFacturacion().setCalle(clienteDTO.getUbicacion().getCalle());
    //      }
    //      if (clienteDTO.getUbicacion().getNumero() != null
    //        && !clienteDTO
    //        .getUbicacion()
    //        .getNumero()
    //        .equals(clientePersistido.getUbicacionFacturacion().getNumero())) {
    //
    // clientePorActualizar.getUbicacionFacturacion().setNumero(clienteDTO.getUbicacion().getNumero());
    //      }
    //      if (clienteDTO.getUbicacion().getPiso() != null
    //        && !clienteDTO
    //        .getUbicacion()
    //        .getPiso()
    //        .equals(clientePersistido.getUbicacionFacturacion().getPiso())) {
    //
    // clientePorActualizar.getUbicacionFacturacion().setPiso(clienteDTO.getUbicacion().getPiso());
    //      }
    //      if (clienteDTO.getUbicacion().getDepartamento() != null
    //        && !clienteDTO
    //        .getUbicacion()
    //        .getDepartamento()
    //        .equals(clientePersistido.getUbicacionFacturacion().getDepartamento())) {
    //        clientePorActualizar
    //          .getUbicacionFacturacion()
    //          .setDepartamento(clienteDTO.getUbicacion().getDepartamento());
    //      }
    if (idEmpresa != null) {
      clientePorActualizar.setEmpresa(empresaService.getEmpresaPorId(idEmpresa));
    } else {
      clientePorActualizar.setEmpresa(clientePersistido.getEmpresa());
    }
    if (idViajante != null) {
      clientePorActualizar.setViajante(usuarioService.getUsuarioPorId(idViajante));
    } else {
      clientePorActualizar.setViajante(null);
    }
    clienteService.actualizar(clientePorActualizar, clientePersistido);
  }

  @PutMapping("/clientes/{idCliente}/predeterminado")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public void setClientePredeterminado(@PathVariable long idCliente) {
    clienteService.setClientePredeterminado(clienteService.getClientePorId(idCliente));
  }

  @GetMapping("/clientes/pedidos/{idPedido}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE})
  public Cliente getClientePorIdPedido(@PathVariable long idPedido) {
    return clienteService.getClientePorIdPedido(idPedido);
  }

  @GetMapping("/clientes/usuarios/{idUsuario}/empresas/{idEmpresa}")
  @AccesoRolesPermitidos({
    Rol.ADMINISTRADOR,
    Rol.ENCARGADO,
    Rol.VENDEDOR,
    Rol.VIAJANTE,
    Rol.COMPRADOR
  })
  public Cliente getClientePorIdUsuario(
      @PathVariable long idUsuario, @PathVariable long idEmpresa) {
    return clienteService.getClientePorIdUsuarioYidEmpresa(idUsuario, idEmpresa);
  }
}
