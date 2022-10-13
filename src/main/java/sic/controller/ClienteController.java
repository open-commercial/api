package sic.controller;

import io.jsonwebtoken.Claims;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import sic.aspect.AccesoRolesPermitidos;
import sic.exception.ForbiddenException;
import sic.modelo.*;
import sic.modelo.criteria.BusquedaClienteCriteria;
import sic.modelo.dto.ClienteDTO;
import sic.service.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Locale;

@RestController
@RequestMapping("/api/v1")
public class ClienteController {

  private final IClienteService clienteService;
  private final IUsuarioService usuarioService;
  private final IUbicacionService ubicacionService;
  private final IAuthService authService;
  private final ModelMapper modelMapper;
  private final MessageSource messageSource;

  @Autowired
  public ClienteController(
      IClienteService clienteService,
      IUsuarioService usuarioService,
      IUbicacionService ubicacionService,
      IAuthService authService,
      ModelMapper modelMapper,
      MessageSource messageSource) {
    this.clienteService = clienteService;
    this.usuarioService = usuarioService;
    this.ubicacionService = ubicacionService;
    this.authService = authService;
    this.modelMapper = modelMapper;
    this.messageSource = messageSource;
  }

  @GetMapping("/clientes/{idCliente}")
  public Cliente getCliente(@PathVariable long idCliente) {
    return clienteService.getClienteNoEliminadoPorId(idCliente);
  }

  @PostMapping("/clientes/busqueda/criteria")
  public Page<Cliente> buscarConCriteria(
      @RequestBody BusquedaClienteCriteria criteria,
      @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    return clienteService.buscarClientes(criteria, (int) claims.get("idUsuario"));
  }

  @GetMapping("/clientes/existe-predeterminado")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE})
  public boolean existeClientePredeterminado() {
    return clienteService.existeClientePredeterminado();
  }

  @DeleteMapping("/clientes/{idCliente}")
  @AccesoRolesPermitidos(Rol.ADMINISTRADOR)
  public void eliminar(@PathVariable long idCliente) {
    clienteService.eliminar(idCliente);
  }

  @PostMapping("/clientes")
  public Cliente guardar(
      @RequestBody ClienteDTO nuevoCliente,
      @RequestHeader("Authorization") String authorizationHeader) {
    Cliente cliente = modelMapper.map(nuevoCliente, Cliente.class);
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    long idUsuarioLoggedIn = (int) claims.get("idUsuario");
    Usuario usuarioLoggedIn = usuarioService.getUsuarioNoEliminadoPorId(idUsuarioLoggedIn);
    if (nuevoCliente.getIdCredencial() != null) {
      if (nuevoCliente.getIdCredencial() != idUsuarioLoggedIn
          && !(usuarioLoggedIn.getRoles().contains(Rol.ADMINISTRADOR)
              || usuarioLoggedIn.getRoles().contains(Rol.ENCARGADO)
              || usuarioLoggedIn.getRoles().contains(Rol.VENDEDOR))) {
        throw new ForbiddenException(
            messageSource.getMessage("mensaje_usuario_rol_no_valido", null, Locale.getDefault()));
      } else {
        Usuario usuarioCredencial =
            usuarioService.getUsuarioNoEliminadoPorId(nuevoCliente.getIdCredencial());
        cliente.setCredencial(usuarioCredencial);
      }
    }
    if (usuarioLoggedIn.getRoles().contains(Rol.ADMINISTRADOR)
        || usuarioLoggedIn.getRoles().contains(Rol.ENCARGADO)
        || usuarioLoggedIn.getRoles().contains(Rol.VENDEDOR)) {
      cliente.setPuedeComprarAPlazo(nuevoCliente.isPuedeComprarAPlazo());
    }
    cliente.setUbicacionFacturacion(null);
    if (nuevoCliente.getUbicacionFacturacion() != null) {
      cliente.setUbicacionFacturacion(
          modelMapper.map(nuevoCliente.getUbicacionFacturacion(), Ubicacion.class));
    }
    cliente.setUbicacionEnvio(null);
    if (nuevoCliente.getUbicacionEnvio() != null) {
      cliente.setUbicacionEnvio(modelMapper.map(nuevoCliente.getUbicacionEnvio(), Ubicacion.class));
    }
    if (nuevoCliente.getIdViajante() != null) {
      cliente.setViajante(usuarioService.getUsuarioNoEliminadoPorId(nuevoCliente.getIdViajante()));
    }
    cliente.setFechaAlta(LocalDateTime.now());
    cliente.setMontoCompraMinima(
        nuevoCliente.getMontoCompraMinima() != null
            ? nuevoCliente.getMontoCompraMinima()
            : BigDecimal.ZERO);
    return clienteService.guardar(cliente);
  }

  @PutMapping("/clientes")
  public Cliente actualizar(
      @RequestBody ClienteDTO clienteDTO,
      @RequestHeader("Authorization") String authorizationHeader) {
    Cliente clientePorActualizar = modelMapper.map(clienteDTO, Cliente.class);
    Cliente clientePersistido =
        clienteService.getClienteNoEliminadoPorId(clientePorActualizar.getIdCliente());
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    long idUsuarioLoggedIn = (int) claims.get("idUsuario");
    Usuario usuarioLoggedIn = usuarioService.getUsuarioNoEliminadoPorId(idUsuarioLoggedIn);
    if (clienteDTO.getIdCredencial() != null) {
      if (clienteDTO.getIdCredencial() != idUsuarioLoggedIn
          && clientePersistido.getCredencial() != null
          && clientePersistido.getCredencial().getIdUsuario() != clienteDTO.getIdCredencial()
          && !(usuarioLoggedIn.getRoles().contains(Rol.ADMINISTRADOR)
              || usuarioLoggedIn.getRoles().contains(Rol.ENCARGADO)
              || usuarioLoggedIn.getRoles().contains(Rol.VENDEDOR))) {
        throw new ForbiddenException(
            messageSource.getMessage("mensaje_usuario_rol_no_valido", null, Locale.getDefault()));
      } else {
        Usuario usuarioCredencial =
            usuarioService.getUsuarioNoEliminadoPorId(clienteDTO.getIdCredencial());
        clientePorActualizar.setCredencial(usuarioCredencial);
      }
    } else {
      clientePorActualizar.setCredencial(clientePersistido.getCredencial());
    }
    if (usuarioLoggedIn.getRoles().contains(Rol.ADMINISTRADOR)
        || usuarioLoggedIn.getRoles().contains(Rol.ENCARGADO)
        || usuarioLoggedIn.getRoles().contains(Rol.VENDEDOR)) {
      clientePorActualizar.setPuedeComprarAPlazo(clienteDTO.isPuedeComprarAPlazo());
    } else {
      clientePorActualizar.setPuedeComprarAPlazo(clientePersistido.isPuedeComprarAPlazo());
    }
    Ubicacion ubicacion;
    if (clienteDTO.getUbicacionFacturacion() != null) {
      ubicacion = modelMapper.map(clienteDTO.getUbicacionFacturacion(), Ubicacion.class);
      ubicacion.setLocalidad(ubicacionService.getLocalidadPorId(ubicacion.getIdLocalidad()));
      clientePorActualizar.setUbicacionFacturacion(ubicacion);
    } else {
      clientePorActualizar.setUbicacionFacturacion(clientePersistido.getUbicacionFacturacion());
    }
    if (clienteDTO.getUbicacionEnvio() != null) {
      ubicacion = modelMapper.map(clienteDTO.getUbicacionEnvio(), Ubicacion.class);
      ubicacion.setLocalidad(ubicacionService.getLocalidadPorId(ubicacion.getIdLocalidad()));
      clientePorActualizar.setUbicacionEnvio(ubicacion);
    } else {
      clientePorActualizar.setUbicacionEnvio(clientePersistido.getUbicacionEnvio());
    }
    if (clienteDTO.getIdViajante() != null) {
      clientePorActualizar.setViajante(usuarioService.getUsuarioNoEliminadoPorId(clienteDTO.getIdViajante()));
    } else {
      clientePorActualizar.setViajante(null);
    }
    if (clientePorActualizar.getMontoCompraMinima() == null)
      clientePorActualizar.setMontoCompraMinima(clientePersistido.getMontoCompraMinima());
    clientePorActualizar.setFechaAlta(clientePersistido.getFechaAlta());
    return clienteService.actualizar(clientePorActualizar, clientePersistido);
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

  @GetMapping("/clientes/usuarios/{idUsuario}")
  public Cliente getClientePorIdUsuario(@PathVariable long idUsuario) {
    return clienteService.getClientePorIdUsuario(idUsuario);
  }
}
