package org.opencommercial.controller;

import io.jsonwebtoken.Claims;
import org.modelmapper.ModelMapper;
import org.opencommercial.aspect.AccesoRolesPermitidos;
import org.opencommercial.exception.ForbiddenException;
import org.opencommercial.model.Cliente;
import org.opencommercial.model.Rol;
import org.opencommercial.model.Ubicacion;
import org.opencommercial.model.Usuario;
import org.opencommercial.model.criteria.BusquedaClienteCriteria;
import org.opencommercial.model.dto.ClienteDTO;
import org.opencommercial.service.AuthService;
import org.opencommercial.service.ClienteService;
import org.opencommercial.service.UbicacionService;
import org.opencommercial.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Locale;

@RestController
public class ClienteController {

  private final ClienteService clienteService;
  private final UsuarioService usuarioService;
  private final UbicacionService ubicacionService;
  private final AuthService authService;
  private final ModelMapper modelMapper;
  private final MessageSource messageSource;
  private static final String CLAIM_ID_USUARIO = "idUsuario";

  @Autowired
  public ClienteController(ClienteService clienteService,
                           UsuarioService usuarioService,
                           UbicacionService ubicacionService,
                           AuthService authService,
                           ModelMapper modelMapper,
                           MessageSource messageSource) {
    this.clienteService = clienteService;
    this.usuarioService = usuarioService;
    this.ubicacionService = ubicacionService;
    this.authService = authService;
    this.modelMapper = modelMapper;
    this.messageSource = messageSource;
  }

  @GetMapping("/api/v1/clientes/{idCliente}")
  public Cliente getCliente(@PathVariable long idCliente) {
    return clienteService.getClienteNoEliminadoPorId(idCliente);
  }

  @PostMapping("/api/v1/clientes/busqueda/criteria")
  public Page<Cliente> buscarConCriteria(@RequestBody BusquedaClienteCriteria criteria,
                                         @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    return clienteService.buscarClientes(criteria, claims.get(CLAIM_ID_USUARIO, Long.class));
  }

  @GetMapping("/api/v1/clientes/existe-predeterminado")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE})
  public boolean existeClientePredeterminado() {
    return clienteService.existeClientePredeterminado();
  }

  @DeleteMapping("/api/v1/clientes/{idCliente}")
  @AccesoRolesPermitidos(Rol.ADMINISTRADOR)
  public void eliminar(@PathVariable long idCliente) {
    clienteService.eliminar(idCliente);
  }

  @PostMapping("/api/v1/clientes")
  public Cliente guardar(@RequestBody ClienteDTO nuevoCliente,
                         @RequestHeader("Authorization") String authorizationHeader) {
    Cliente cliente = modelMapper.map(nuevoCliente, Cliente.class);
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    long idUsuarioLoggedIn = claims.get(CLAIM_ID_USUARIO, Long.class);
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
    if (nuevoCliente.getPuedeComprarAPlazo() != null && (usuarioLoggedIn.getRoles().contains(Rol.ADMINISTRADOR)
        || usuarioLoggedIn.getRoles().contains(Rol.ENCARGADO)
        || usuarioLoggedIn.getRoles().contains(Rol.VENDEDOR))) {
      cliente.setPuedeComprarAPlazo(nuevoCliente.getPuedeComprarAPlazo());
    } else {
      cliente.setPuedeComprarAPlazo(false);
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

  @PutMapping("/api/v1/clientes")
  public Cliente actualizar(@RequestBody ClienteDTO clienteDTO,
                            @RequestHeader("Authorization") String authorizationHeader) {
    Cliente clientePorActualizar = modelMapper.map(clienteDTO, Cliente.class);
    Cliente clientePersistido = clienteService.getClienteNoEliminadoPorId(clientePorActualizar.getIdCliente());
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    long idUsuarioLoggedIn = claims.get(CLAIM_ID_USUARIO, Long.class);
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
    if (clienteDTO.getPuedeComprarAPlazo() != null && (usuarioLoggedIn.getRoles().contains(Rol.ADMINISTRADOR)
            || usuarioLoggedIn.getRoles().contains(Rol.ENCARGADO)
            || usuarioLoggedIn.getRoles().contains(Rol.VENDEDOR))) {
      clientePorActualizar.setPuedeComprarAPlazo(clienteDTO.getPuedeComprarAPlazo());
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

  @PutMapping("/api/v1/clientes/{idCliente}/predeterminado")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public void setClientePredeterminado(@PathVariable long idCliente) {
    clienteService.setClientePredeterminado(clienteService.getClienteNoEliminadoPorId(idCliente));
  }

  @GetMapping("/api/v1/clientes/usuarios/{idUsuario}")
  public Cliente getClientePorIdUsuario(@PathVariable long idUsuario) {
    return clienteService.getClientePorIdUsuario(idUsuario);
  }
}
