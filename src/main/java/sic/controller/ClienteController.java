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
  private final IUsuarioService usuarioService;
  private final IAuthService authService;
  private final ModelMapper modelMapper;
  private static final int TAMANIO_PAGINA_DEFAULT = 25;

  @Autowired
  public ClienteController(
      IClienteService clienteService,
      IEmpresaService empresaService,
      IUsuarioService usuarioService,
      IAuthService authService,
      ModelMapper modelMapper) {
    this.clienteService = clienteService;
    this.empresaService = empresaService;
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
    return clienteService.getClienteNoEliminadoPorId(idCliente);
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
          PageRequest.of(
              pagina, TAMANIO_PAGINA_DEFAULT, new Sort(Sort.Direction.ASC, "nombreFiscal"));
    } else {
      switch (sentido) {
        case "ASC":
          pageable =
              PageRequest.of(
                  pagina, TAMANIO_PAGINA_DEFAULT, new Sort(Sort.Direction.ASC, ordenarPor));
          break;
        case "DESC":
          pageable =
              PageRequest.of(
                  pagina, TAMANIO_PAGINA_DEFAULT, new Sort(Sort.Direction.DESC, ordenarPor));
          break;
        default:
          pageable =
              PageRequest.of(
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
          && !(usuarioLoggedIn.getRoles().contains(Rol.ADMINISTRADOR)
              || usuarioLoggedIn.getRoles().contains(Rol.ENCARGADO)
              || usuarioLoggedIn.getRoles().contains(Rol.VENDEDOR))) {
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
    cliente.setUbicacionFacturacion(null);
    cliente.setUbicacionEnvio(null);
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
        clienteService.getClienteNoEliminadoPorId(clientePorActualizar.getId_Cliente());
    if (idCredencial != null) {
      Claims claims = authService.getClaimsDelToken(authorizationHeader);
      long idUsuarioLoggedIn = (int) claims.get("idUsuario");
      Usuario usuarioLoggedIn = usuarioService.getUsuarioPorId(idUsuarioLoggedIn);
      if (idCredencial != idUsuarioLoggedIn
          && clientePersistido.getCredencial() != null
          && clientePersistido.getCredencial().getId_Usuario() != idCredencial
          && !(usuarioLoggedIn.getRoles().contains(Rol.ADMINISTRADOR)
              || usuarioLoggedIn.getRoles().contains(Rol.ENCARGADO)
              || usuarioLoggedIn.getRoles().contains(Rol.VENDEDOR))) {
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
    clientePorActualizar.setUbicacionFacturacion(clientePersistido.getUbicacionFacturacion());
    clientePorActualizar.setUbicacionEnvio(clientePersistido.getUbicacionEnvio());
    clienteService.actualizar(clientePorActualizar, clientePersistido);
  }

  @PutMapping("/clientes/{idCliente}/predeterminado")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public void setClientePredeterminado(@PathVariable long idCliente) {
    clienteService.setClientePredeterminado(clienteService.getClienteNoEliminadoPorId(idCliente));
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
