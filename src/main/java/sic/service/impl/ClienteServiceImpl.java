package sic.service.impl;

import com.querydsl.core.BooleanBuilder;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Date;
import java.util.ResourceBundle;
import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import sic.modelo.*;
import sic.service.*;
import sic.util.Validator;
import sic.repository.ClienteRepository;

@Service
@Validated
public class ClienteServiceImpl implements IClienteService {

  private final ClienteRepository clienteRepository;
  private final ICuentaCorrienteService cuentaCorrienteService;
  private final IUsuarioService usuarioService;
  private final IUbicacionService ubicacionService;
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("Mensajes");

  @Autowired
  public ClienteServiceImpl(
      ClienteRepository clienteRepository,
      ICuentaCorrienteService cuentaCorrienteService,
      IUsuarioService usuarioService,
      IUbicacionService ubicacionService) {
    this.clienteRepository = clienteRepository;
    this.cuentaCorrienteService = cuentaCorrienteService;
    this.usuarioService = usuarioService;
    this.ubicacionService = ubicacionService;
  }

  @Override
  public Cliente getClientePorId(long idCliente) {
    Cliente cliente = clienteRepository.findOne(idCliente);
    if (cliente == null) {
      throw new EntityNotFoundException(RESOURCE_BUNDLE.getString("mensaje_cliente_no_existente"));
    }
    return cliente;
  }

  @Override
  public Cliente getClientePorIdFiscal(Long idFiscal, Empresa empresa) {
    return clienteRepository.findByIdFiscalAndEmpresaAndEliminado(idFiscal, empresa, false);
  }

  @Override
  public Cliente getClientePredeterminado(Empresa empresa) {
    Cliente cliente =
        clienteRepository.findByEmpresaAndPredeterminadoAndEliminado(empresa, true, false);
    if (cliente == null) {
      throw new EntityNotFoundException(
          RESOURCE_BUNDLE.getString("mensaje_cliente_sin_predeterminado"));
    }
    return cliente;
  }

  @Override
  public boolean existeClientePredeterminado(Empresa empresa) {
    return clienteRepository.existsByEmpresaAndPredeterminadoAndEliminado(empresa, true, false);
  }

  @Override
  @Transactional
  public void setClientePredeterminado(Cliente cliente) {
    Cliente clientePredeterminadoAnterior =
        clienteRepository.findByEmpresaAndPredeterminadoAndEliminado(
            cliente.getEmpresa(), true, false);
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
    if (criteria.isBuscaPorNombreFiscal()) {
      String[] terminos = criteria.getNombreFiscal().split(" ");
      BooleanBuilder rsPredicate = new BooleanBuilder();
      for (String termino : terminos) {
        rsPredicate.and(qCliente.nombreFiscal.containsIgnoreCase(termino));
      }
      builder.or(rsPredicate);
    }
    if (criteria.isBuscaPorNombreFantasia()) {
      String[] terminos = criteria.getNombreFantasia().split(" ");
      BooleanBuilder nfPredicate = new BooleanBuilder();
      for (String termino : terminos) {
        nfPredicate.and(qCliente.nombreFantasia.containsIgnoreCase(termino));
      }
      builder.or(nfPredicate);
    }
    if (criteria.isBuscaPorIdFiscal()) builder.or(qCliente.idFiscal.eq(criteria.getIdFiscal()));
    if (criteria.isBuscarPorNroDeCliente())
      builder.or(qCliente.nroCliente.containsIgnoreCase(criteria.getNroDeCliente()));
    if (criteria.isBuscaPorViajante())
      builder.and(qCliente.viajante.id_Usuario.eq(criteria.getIdViajante()));
    if (criteria.isBuscaPorLocalidad())
      builder.and(
          qCliente.ubicacionFacturacion.localidad.idLocalidad.eq(criteria.getIdLocalidad()));
    if (criteria.isBuscaPorProvincia())
      builder.and(
          qCliente.ubicacionFacturacion.localidad.provincia.idProvincia.eq(
              criteria.getIdProvincia()));
    if (criteria.isBuscaPorLocalidad())
      builder.and(qCliente.ubicacionEnvio.localidad.idLocalidad.eq(criteria.getIdLocalidad()));
    if (criteria.isBuscaPorProvincia())
      builder.and(
          qCliente.ubicacionEnvio.localidad.provincia.idProvincia.eq(criteria.getIdProvincia()));
    Usuario usuarioLogueado = usuarioService.getUsuarioPorId(idUsuarioLoggedIn);
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
            Cliente clienteRelacionado =
                this.getClientePorIdUsuarioYidEmpresa(idUsuarioLoggedIn, criteria.getIdEmpresa());
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
    builder.and(
        qCliente.empresa.id_Empresa.eq(criteria.getIdEmpresa()).and(qCliente.eliminado.eq(false)));
    return clienteRepository.findAll(builder, criteria.getPageable());
  }

  @Override
  public void validarOperacion(TipoDeOperacion operacion, Cliente cliente) {
    // Requeridos
    if (operacion == TipoDeOperacion.ALTA && cliente.getCredencial() == null) {
      throw new BusinessServiceException(
          RESOURCE_BUNDLE.getString("mensaje_cliente_vacio_credencial"));
    }
    // Duplicados
    // ID Fiscal
    if (cliente.getIdFiscal() != null) {
      Cliente clienteDuplicado =
          this.getClientePorIdFiscal(cliente.getIdFiscal(), cliente.getEmpresa());
      if (operacion == TipoDeOperacion.ACTUALIZACION
          && clienteDuplicado != null
          && clienteDuplicado.getId_Cliente() != cliente.getId_Cliente()) {
        throw new BusinessServiceException(
            RESOURCE_BUNDLE.getString("mensaje_cliente_duplicado_idFiscal"));
      }
      if (operacion == TipoDeOperacion.ALTA
          && clienteDuplicado != null
          && cliente.getIdFiscal() != null) {
        throw new BusinessServiceException(
            RESOURCE_BUNDLE.getString("mensaje_cliente_duplicado_idFiscal"));
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
      throw new BusinessServiceException(
          RESOURCE_BUNDLE.getString("mensaje_ubicacion_facturacion_envio_iguales"));
    }
    if (cliente.getUbicacionFacturacion() != null
        && cliente.getUbicacionFacturacion().getLocalidad() == null) {
      throw new BusinessServiceException(
          RESOURCE_BUNDLE.getString("mensaje_ubicacion_facturacion_sin_localidad"));
    }
    if (cliente.getUbicacionEnvio() != null
      && cliente.getUbicacionEnvio().getLocalidad() == null) {
      throw new BusinessServiceException(
        RESOURCE_BUNDLE.getString("mensaje_ubicacion_envio_sin_localidad"));
    }
  }

  @Override
  @Transactional
  public Cliente guardar(Cliente cliente) {
    cliente.setFechaAlta(new Date());
    cliente.setEliminado(false);
    cliente.setNroCliente(this.generarNroDeCliente(cliente.getEmpresa()));
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
    cuentaCorrienteCliente.setEmpresa(cliente.getEmpresa());
    cuentaCorrienteCliente.setFechaApertura(cliente.getFechaAlta());
    cuentaCorrienteCliente.setSaldo(BigDecimal.ZERO);
    if (cliente.getCredencial() != null) {
      Cliente clienteYaAsignado =
          this.getClientePorIdUsuarioYidEmpresa(
              cliente.getCredencial().getId_Usuario(), cliente.getEmpresa().getId_Empresa());
      if (clienteYaAsignado != null) {
        throw new BusinessServiceException(
            MessageFormat.format(
                RESOURCE_BUNDLE.getString("mensaje_cliente_credencial_no_valida"),
                clienteYaAsignado.getNombreFiscal()));
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
  public void actualizar(@Valid Cliente clientePorActualizar, Cliente clientePersistido) {
    clientePorActualizar.setNroCliente(clientePersistido.getNroCliente());
    clientePorActualizar.setFechaAlta(clientePersistido.getFechaAlta());
    clientePorActualizar.setPredeterminado(clientePersistido.isPredeterminado());
    clientePorActualizar.setEliminado(clientePersistido.isEliminado());
    if (clientePorActualizar.getBonificacion() == null)
      clientePorActualizar.setBonificacion(BigDecimal.ZERO);
    this.validarOperacion(TipoDeOperacion.ACTUALIZACION, clientePorActualizar);
    if (clientePorActualizar.getCredencial() != null) {
      Cliente clienteYaAsignado =
          this.getClientePorIdUsuarioYidEmpresa(
              clientePorActualizar.getCredencial().getId_Usuario(),
              clientePorActualizar.getEmpresa().getId_Empresa());
      if (clienteYaAsignado != null
          && clienteYaAsignado.getId_Cliente() != clientePorActualizar.getId_Cliente()) {
        throw new BusinessServiceException(
            MessageFormat.format(
                RESOURCE_BUNDLE.getString("mensaje_cliente_credencial_no_valida"),
                clienteYaAsignado.getNombreFiscal()));
      } else {
        if (!clientePorActualizar.getCredencial().getRoles().contains(Rol.COMPRADOR)) {
          clientePorActualizar.getCredencial().getRoles().add(Rol.COMPRADOR);
        }
      }
    }
    clienteRepository.save(clientePorActualizar);
    logger.warn("El Cliente {} se actualizó correctamente.", clientePorActualizar);
  }

  @Override
  @Transactional
  public void eliminar(long idCliente) {
    Cliente cliente = this.getClientePorId(idCliente);
    if (cliente == null) {
      throw new EntityNotFoundException(RESOURCE_BUNDLE.getString("mensaje_cliente_no_existente"));
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
  public Cliente getClientePorIdUsuarioYidEmpresa(long idUsuario, long idEmpresa) {
    return clienteRepository.findClienteByIdUsuarioYidEmpresa(idUsuario, idEmpresa);
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
  public String generarNroDeCliente(Empresa empresa) {
    long min = 1L;
    long max = 99999L; // 5 digitos
    long randomLong = 0L;
    boolean esRepetido = true;
    while (esRepetido) {
      randomLong = min + (long) (Math.random() * (max - min));
      String nroCliente = Long.toString(randomLong);
      Cliente c =
          clienteRepository.findByNroClienteAndEmpresaAndEliminado(nroCliente, empresa, false);
      if (c == null) esRepetido = false;
    }
    return Long.toString(randomLong);
  }
}
