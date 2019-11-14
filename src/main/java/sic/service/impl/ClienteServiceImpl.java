package sic.service.impl;

import com.querydsl.core.BooleanBuilder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import sic.modelo.*;
import sic.modelo.criteria.BusquedaClienteCriteria;
import sic.service.*;
import sic.repository.ClienteRepository;
import sic.exception.BusinessServiceException;

@Service
@Validated
public class ClienteServiceImpl implements IClienteService {

  private final ClienteRepository clienteRepository;
  private final ICuentaCorrienteService cuentaCorrienteService;
  private final IUsuarioService usuarioService;
  private final IUbicacionService ubicacionService;
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private static final int TAMANIO_PAGINA_DEFAULT = 25;
  private final MessageSource messageSource;

  @Autowired
  public ClienteServiceImpl(
      ClienteRepository clienteRepository,
      ICuentaCorrienteService cuentaCorrienteService,
      IUsuarioService usuarioService,
      IUbicacionService ubicacionService,
      MessageSource messageSource) {
    this.clienteRepository = clienteRepository;
    this.cuentaCorrienteService = cuentaCorrienteService;
    this.usuarioService = usuarioService;
    this.ubicacionService = ubicacionService;
    this.messageSource = messageSource;
  }

  @Override
  public Cliente getClienteNoEliminadoPorId(long idCliente) {
    Optional<Cliente> cliente = clienteRepository.findById(idCliente);
    if (cliente.isPresent() && !cliente.get().isEliminado()) {
      return cliente.get();
    } else {
      throw new EntityNotFoundException(messageSource.getMessage(
        "mensaje_cliente_no_existente", null, Locale.getDefault()));
    }
  }

  @Override
  public Cliente getClientePredeterminado() {
    Cliente cliente =
        clienteRepository.findByAndPredeterminadoAndEliminado(true, false);
    if (cliente == null) {
      throw new EntityNotFoundException(messageSource.getMessage(
        "mensaje_cliente_sin_predeterminado", null, Locale.getDefault()));
    }
    return cliente;
  }

  @Override
  public boolean existeClientePredeterminado() {
    return clienteRepository.existsByAndPredeterminadoAndEliminado(true, false);
  }

  @Override
  @Transactional
  public void setClientePredeterminado(Cliente cliente) {
    Cliente clientePredeterminadoAnterior =
        clienteRepository.findByAndPredeterminadoAndEliminado(true, false);
    if (clientePredeterminadoAnterior != null) {
      clientePredeterminadoAnterior.setPredeterminado(false);
      clienteRepository.save(clientePredeterminadoAnterior);
    }
    cliente.setPredeterminado(true);
    clienteRepository.save(cliente);
  }

  @Override
  public Page<Cliente> buscarClientes(BusquedaClienteCriteria criteria, long idUsuarioLoggedIn) {
    QCliente qCliente = QCliente.cliente;
    BooleanBuilder builder = new BooleanBuilder();
    if (criteria.getNombreFiscal() != null) {
      String[] terminos = criteria.getNombreFiscal().split(" ");
      BooleanBuilder rsPredicate = new BooleanBuilder();
      for (String termino : terminos) {
        rsPredicate.and(qCliente.nombreFiscal.containsIgnoreCase(termino));
      }
      builder.or(rsPredicate);
    }
    if (criteria.getNombreFantasia() != null) {
      String[] terminos = criteria.getNombreFantasia().split(" ");
      BooleanBuilder nfPredicate = new BooleanBuilder();
      for (String termino : terminos) {
        nfPredicate.and(qCliente.nombreFantasia.containsIgnoreCase(termino));
      }
      builder.or(nfPredicate);
    }
    if (criteria.getIdFiscal() != null) builder.or(qCliente.idFiscal.eq(criteria.getIdFiscal()));
    if (criteria.getNroDeCliente() != null)
      builder.or(qCliente.nroCliente.containsIgnoreCase(criteria.getNroDeCliente()));
    if (criteria.getIdViajante() != null)
      builder.and(qCliente.viajante.idUsuario.eq(criteria.getIdViajante()));
    if (criteria.getIdLocalidad() != null)
      builder.and(
          qCliente.ubicacionFacturacion.localidad.idLocalidad.eq(criteria.getIdLocalidad()));
    if (criteria.getIdProvincia() != null)
      builder.and(
          qCliente.ubicacionFacturacion.localidad.provincia.idProvincia.eq(
              criteria.getIdProvincia()));
    if (criteria.getIdLocalidad() != null)
      builder.and(qCliente.ubicacionEnvio.localidad.idLocalidad.eq(criteria.getIdLocalidad()));
    if (criteria.getIdProvincia() != null)
      builder.and(
          qCliente.ubicacionEnvio.localidad.provincia.idProvincia.eq(criteria.getIdProvincia()));
    Usuario usuarioLogueado = usuarioService.getUsuarioNoEliminadoPorId(idUsuarioLoggedIn);
    if (!usuarioLogueado.getRoles().contains(Rol.ADMINISTRADOR)
        && !usuarioLogueado.getRoles().contains(Rol.VENDEDOR)
        && !usuarioLogueado.getRoles().contains(Rol.ENCARGADO)) {
      BooleanBuilder rsPredicate = new BooleanBuilder();
      for (Rol rol : usuarioLogueado.getRoles()) {
        switch (rol) {
          case VIAJANTE:
            rsPredicate.or(qCliente.viajante.eq(usuarioLogueado));
            break;
          case COMPRADOR:
            Cliente clienteRelacionado = this.getClientePorIdUsuario(idUsuarioLoggedIn);
            if (clienteRelacionado != null) {
              rsPredicate.or(qCliente.eq(clienteRelacionado));
            }
            break;
          default:
            rsPredicate.or(qCliente.isNull());
            break;
        }
      }
      builder.and(rsPredicate);
    }
    builder.and(qCliente.eliminado.eq(false));
    return clienteRepository.findAll(
        builder,
        this.getPageable(criteria.getPagina(), criteria.getOrdenarPor(), criteria.getSentido()));
  }

  private Pageable getPageable(int pagina, String ordenarPor, String sentido) {
    String ordenDefault = "nombreFiscal";
    if (ordenarPor == null || sentido == null) {
      return PageRequest.of(
          pagina, TAMANIO_PAGINA_DEFAULT, new Sort(Sort.Direction.ASC, ordenDefault));
    } else {
      switch (sentido) {
        case "ASC":
          return PageRequest.of(
              pagina, TAMANIO_PAGINA_DEFAULT, new Sort(Sort.Direction.ASC, ordenarPor));
        case "DESC":
          return PageRequest.of(
              pagina, TAMANIO_PAGINA_DEFAULT, new Sort(Sort.Direction.DESC, ordenarPor));
        default:
          return PageRequest.of(
              pagina, TAMANIO_PAGINA_DEFAULT, new Sort(Sort.Direction.DESC, ordenDefault));
      }
    }
  }

  @Override
  public void validarOperacion(TipoDeOperacion operacion, Cliente cliente) {
    // Requeridos
    if (operacion == TipoDeOperacion.ALTA && cliente.getCredencial() == null) {
      throw new BusinessServiceException(messageSource.getMessage(
        "mensaje_cliente_vacio_credencial", null, Locale.getDefault()));
    }
    // Duplicados
    // ID Fiscal
    if (cliente.getIdFiscal() != null) {
      List<Cliente> clientes =
          clienteRepository.findByIdFiscalAndEliminado(cliente.getIdFiscal(), false);
      if (clientes.size() > 1
          || operacion == TipoDeOperacion.ACTUALIZACION
              && !clientes.isEmpty()
              && clientes.get(0).getIdCliente() != cliente.getIdCliente()) {
        throw new BusinessServiceException(
            messageSource.getMessage(
                "mensaje_cliente_duplicado_idFiscal", null, Locale.getDefault()));
      }
      if (operacion == TipoDeOperacion.ALTA
          && !clientes.isEmpty()
          && cliente.getIdFiscal() != null) {
        throw new BusinessServiceException(
            messageSource.getMessage(
                "mensaje_cliente_duplicado_idFiscal", null, Locale.getDefault()));
      }
    }
    // Ubicacion
    if (cliente.getUbicacionFacturacion() != null
        && cliente.getUbicacionEnvio() != null
        && operacion == TipoDeOperacion.ACTUALIZACION
        && (cliente.getUbicacionFacturacion().getIdUbicacion() != 0L)
        && (cliente.getUbicacionEnvio().getIdUbicacion() != 0L)
        && (cliente.getUbicacionFacturacion().getIdUbicacion()
            == cliente.getUbicacionEnvio().getIdUbicacion())) {
      throw new BusinessServiceException(messageSource.getMessage(
        "mensaje_ubicacion_facturacion_envio_iguales", null, Locale.getDefault()));
    }
    if (cliente.getUbicacionFacturacion() != null
        && cliente.getUbicacionFacturacion().getLocalidad() == null) {
      throw new BusinessServiceException(messageSource.getMessage(
        "mensaje_ubicacion_facturacion_sin_localidad", null, Locale.getDefault()));
    }
    if (cliente.getUbicacionEnvio() != null
      && cliente.getUbicacionEnvio().getLocalidad() == null) {
      throw new BusinessServiceException(messageSource.getMessage(
        "mensaje_ubicacion_envio_sin_localidad", null, Locale.getDefault()));
    }
  }

  @Override
  @Transactional
  public Cliente guardar(@Valid Cliente cliente) {
    cliente.setEliminado(false);
    cliente.setNroCliente(this.generarNroDeCliente());
    if (cliente.getBonificacion() == null) cliente.setBonificacion(BigDecimal.ZERO);
    if (cliente.getUbicacionFacturacion() != null
        && cliente.getUbicacionFacturacion().getIdLocalidad() != null) {
      cliente
          .getUbicacionFacturacion()
          .setLocalidad(
              ubicacionService.getLocalidadPorId(
                  cliente.getUbicacionFacturacion().getIdLocalidad()));
    }
    if (cliente.getUbicacionEnvio() != null
        && cliente.getUbicacionEnvio().getIdLocalidad() != null) {
      cliente
          .getUbicacionEnvio()
          .setLocalidad(
              ubicacionService.getLocalidadPorId(cliente.getUbicacionEnvio().getIdLocalidad()));
    }
    this.validarOperacion(TipoDeOperacion.ALTA, cliente);
    CuentaCorrienteCliente cuentaCorrienteCliente = new CuentaCorrienteCliente();
    cuentaCorrienteCliente.setCliente(cliente);
    cuentaCorrienteCliente.setFechaApertura(cliente.getFechaAlta());
    cuentaCorrienteCliente.setFechaUltimoMovimiento(cliente.getFechaAlta());
    cuentaCorrienteCliente.setSaldo(BigDecimal.ZERO);
    if (cliente.getCredencial() != null) {
      Cliente clienteYaAsignado = this.getClientePorIdUsuario(cliente.getCredencial().getIdUsuario());
      if (clienteYaAsignado != null) {
        throw new BusinessServiceException(messageSource.getMessage(
          "mensaje_cliente_credencial_no_valida", new Object[] {clienteYaAsignado.getNombreFiscal()}, Locale.getDefault()));
      } else {
        if (!cliente.getCredencial().getRoles().contains(Rol.COMPRADOR)) {
          cliente.getCredencial().getRoles().add(Rol.COMPRADOR);
        }
      }
    }
    cuentaCorrienteCliente.setFechaApertura(cuentaCorrienteCliente.getCliente().getFechaAlta());
    cliente = clienteRepository.save(cliente);
    cuentaCorrienteService.guardarCuentaCorrienteCliente(cuentaCorrienteCliente);
    logger.warn("El Cliente {} se guardó correctamente.", cliente);
    return cliente;
  }

  @Override
  @Transactional
  public Cliente actualizar(@Valid Cliente clientePorActualizar, Cliente clientePersistido) {
    clientePorActualizar.setNroCliente(clientePersistido.getNroCliente());
    clientePorActualizar.setFechaAlta(clientePersistido.getFechaAlta());
    clientePorActualizar.setPredeterminado(clientePersistido.isPredeterminado());
    clientePorActualizar.setEliminado(clientePersistido.isEliminado());
    if (clientePorActualizar.getBonificacion() == null)
      clientePorActualizar.setBonificacion(BigDecimal.ZERO);
    this.validarOperacion(TipoDeOperacion.ACTUALIZACION, clientePorActualizar);
    if (clientePorActualizar.getCredencial() != null) {
      Cliente clienteYaAsignado =
          this.getClientePorIdUsuario(clientePorActualizar.getCredencial().getIdUsuario());
      if (clienteYaAsignado != null
          && clienteYaAsignado.getIdCliente() != clientePorActualizar.getIdCliente()) {
        throw new BusinessServiceException(messageSource.getMessage(
          "mensaje_cliente_credencial_no_valida", new Object[] {clienteYaAsignado.getNombreFiscal()}, Locale.getDefault()));
      } else {
        if (!clientePorActualizar.getCredencial().getRoles().contains(Rol.COMPRADOR)) {
          clientePorActualizar.getCredencial().getRoles().add(Rol.COMPRADOR);
        }
      }
    }
    Cliente clienteGuardado = clienteRepository.save(clientePorActualizar);
    logger.warn("El Cliente {} se actualizó correctamente.", clienteGuardado);
    return clienteGuardado;
  }

  @Override
  @Transactional
  public void eliminar(long idCliente) {
    Cliente cliente = this.getClienteNoEliminadoPorId(idCliente);
    if (cliente == null) {
      throw new EntityNotFoundException(messageSource.getMessage(
        "mensaje_cliente_no_existente", null, Locale.getDefault()));
    }
    cuentaCorrienteService.eliminarCuentaCorrienteCliente(idCliente);
    cliente.setCredencial(null);
    cliente.setEliminado(true);
    cliente.setUbicacionFacturacion(null);
    cliente.setUbicacionEnvio(null);
    clienteRepository.save(cliente);
    logger.warn("El Cliente {} se eliminó correctamente.", cliente);
  }

  @Override
  public Cliente getClientePorIdPedido(long idPedido) {
    return clienteRepository.findClienteByIdPedido(idPedido);
  }

  @Override
  public Cliente getClientePorIdUsuario(long idUsuario) {
    return clienteRepository.findClienteByIdUsuario(idUsuario);
  }

  @Override
  public int desvincularClienteDeViajante(long idUsuarioViajante) {
    return clienteRepository.desvincularClienteDeViajante(idUsuarioViajante);
  }

  @Override
  public int desvincularClienteDeCredencial(long idUsuarioCliente) {
    return clienteRepository.desvincularClienteDeCredencial(idUsuarioCliente);
  }

  @Override
  public Cliente getClientePorCredencial(Usuario usuarioCredencial) {
    return clienteRepository.findByCredencialAndEliminado(usuarioCredencial, false);
  }

  @Override
  public String generarNroDeCliente() {
    long min = 1L;
    long max = 99999L; // 5 digitos
    long randomLong = 0L;
    boolean esRepetido = true;
    while (esRepetido) {
      randomLong = min + (long) (Math.random() * (max - min));
      String nroCliente = Long.toString(randomLong);
      Cliente c = clienteRepository.findByNroClienteAndEliminado(nroCliente, false);
      if (c == null) esRepetido = false;
    }
    return Long.toString(randomLong);
  }
}
