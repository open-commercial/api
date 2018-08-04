package sic.service.impl;

import com.querydsl.core.BooleanBuilder;

import java.text.MessageFormat;
import java.util.Date;
import java.util.ResourceBundle;
import javax.persistence.EntityNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sic.modelo.*;
import sic.service.IClienteService;
import sic.service.BusinessServiceException;
import sic.service.IUsuarioService;
import sic.util.Validator;
import sic.repository.ClienteRepository;
import sic.service.ICuentaCorrienteService;

@Service
public class ClienteServiceImpl implements IClienteService {

  private final ClienteRepository clienteRepository;
  private final ICuentaCorrienteService cuentaCorrienteService;
  private final IUsuarioService usuarioService;
  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Autowired
  public ClienteServiceImpl(
      ClienteRepository clienteRepository,
      ICuentaCorrienteService cuentaCorrienteService,
      IUsuarioService usuarioService) {
    this.clienteRepository = clienteRepository;
    this.cuentaCorrienteService = cuentaCorrienteService;
    this.usuarioService = usuarioService;
  }

  @Override
  public Cliente getClientePorId(long idCliente) {
    Cliente cliente = clienteRepository.findOne(idCliente);
    if (cliente == null) {
      throw new EntityNotFoundException(
          ResourceBundle.getBundle("Mensajes").getString("mensaje_cliente_no_existente"));
    }
    return cliente;
  }

  @Override
  public Cliente getClientePorRazonSocial(String razonSocial, Empresa empresa) {
    return clienteRepository.findByRazonSocialAndEmpresaAndEliminado(razonSocial, empresa, false);
  }

  @Override
  public Cliente getClientePorIdFiscal(String idFiscal, Empresa empresa) {
    return clienteRepository.findByIdFiscalAndEmpresaAndEliminado(idFiscal, empresa, false);
  }

  @Override
  public Cliente getClientePredeterminado(Empresa empresa) {
    Cliente cliente =
        clienteRepository.findByEmpresaAndPredeterminadoAndEliminado(empresa, true, false);
    if (cliente == null) {
      throw new EntityNotFoundException(
          ResourceBundle.getBundle("Mensajes").getString("mensaje_cliente_sin_predeterminado"));
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
    if (criteria.isBuscaPorRazonSocial()) {
      String[] terminos = criteria.getRazonSocial().split(" ");
      BooleanBuilder rsPredicate = new BooleanBuilder();
      for (String termino : terminos) {
        rsPredicate.and(qCliente.razonSocial.containsIgnoreCase(termino));
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
    if (criteria.isBuscaPorId_Fiscal()) builder.or(qCliente.idFiscal.containsIgnoreCase(criteria.getIdFiscal()));
    if (criteria.isBuscarPorNroDeCliente()) builder.or(qCliente.nroCliente.containsIgnoreCase(criteria.getNroDeCliente()));
    if (criteria.isBuscaPorViajante()) builder.and(qCliente.viajante.id_Usuario.eq(criteria.getIdViajante()));
    if (criteria.isBuscaPorLocalidad()) builder.and(qCliente.localidad.id_Localidad.eq(criteria.getIdLocalidad()));
    if (criteria.isBuscaPorProvincia())
      builder.and(qCliente.localidad.provincia.id_Provincia.eq(criteria.getIdProvincia()));
    if (criteria.isBuscaPorPais())
      builder.and(qCliente.localidad.provincia.pais.id_Pais.eq(criteria.getIdPais()));
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
    builder.and(qCliente.empresa.id_Empresa.eq(criteria.getIdEmpresa()).and(qCliente.eliminado.eq(false)));
    Page<Cliente> page = clienteRepository.findAll(builder, criteria.getPageable());
    if (criteria.isConSaldo()) {
      page.getContent()
          .forEach(
              c -> {
                CuentaCorriente cc = cuentaCorrienteService.getCuentaCorrientePorCliente(c);
                c.setSaldoCuentaCorriente(cc.getSaldo());
                c.setFechaUltimoMovimiento(cc.getFechaUltimoMovimiento());
              });
    }
    return page;
  }

  @Override
  public void validarOperacion(TipoDeOperacion operacion, Cliente cliente) {
    // Entrada de Datos
    if (cliente.getEmail() != null && !cliente.getEmail().equals("")) {
      if (!Validator.esEmailValido(cliente.getEmail())) {
        throw new BusinessServiceException(
            ResourceBundle.getBundle("Mensajes").getString("mensaje_cliente_email_invalido"));
      }
    }
    // Requeridos
    if (Validator.esVacio(cliente.getRazonSocial())) {
      throw new BusinessServiceException(
          ResourceBundle.getBundle("Mensajes").getString("mensaje_cliente_vacio_razonSocial"));
    }
    if (cliente.getCondicionIVA() == null) {
      throw new BusinessServiceException(
          ResourceBundle.getBundle("Mensajes").getString("mensaje_cliente_vacio_condicionIVA"));
    }
    if (cliente.getLocalidad() == null) {
      throw new BusinessServiceException(
          ResourceBundle.getBundle("Mensajes").getString("mensaje_cliente_vacio_localidad"));
    }
    if (cliente.getEmpresa() == null) {
      throw new BusinessServiceException(
          ResourceBundle.getBundle("Mensajes").getString("mensaje_cliente_vacio_empresa"));
    }
    // Duplicados
    // ID Fiscal
    if (cliente.getIdFiscal() != null && !cliente.getIdFiscal().equals("")) {
      Cliente clienteDuplicado =
          this.getClientePorIdFiscal(cliente.getIdFiscal(), cliente.getEmpresa());
      if (operacion.equals(TipoDeOperacion.ACTUALIZACION)
          && clienteDuplicado != null
          && clienteDuplicado.getId_Cliente() != cliente.getId_Cliente()) {
        throw new BusinessServiceException(
            ResourceBundle.getBundle("Mensajes").getString("mensaje_cliente_duplicado_idFiscal"));
      }
      if (operacion.equals(TipoDeOperacion.ALTA)
          && clienteDuplicado != null
          && !cliente.getIdFiscal().equals("")) {
        throw new BusinessServiceException(
            ResourceBundle.getBundle("Mensajes").getString("mensaje_cliente_duplicado_idFiscal"));
      }
    }
    // Razon Social
    Cliente clienteDuplicado =
        this.getClientePorRazonSocial(cliente.getRazonSocial(), cliente.getEmpresa());
    if (operacion.equals(TipoDeOperacion.ALTA) && clienteDuplicado != null) {
      throw new BusinessServiceException(
          ResourceBundle.getBundle("Mensajes").getString("mensaje_cliente_duplicado_razonSocial"));
    }
    if (operacion.equals(TipoDeOperacion.ACTUALIZACION)) {
      if (clienteDuplicado != null && clienteDuplicado.getId_Cliente() != cliente.getId_Cliente()) {
        throw new BusinessServiceException(
            ResourceBundle.getBundle("Mensajes")
                .getString("mensaje_cliente_duplicado_razonSocial"));
      }
    }
  }

  @Override
  @Transactional
  public Cliente guardar(Cliente cliente) {
    cliente.setFechaAlta(new Date());
    cliente.setEliminado(false);
    cliente.setNroCliente(this.calcularNroDeCliente(cliente.getEmpresa()));
    this.validarOperacion(TipoDeOperacion.ALTA, cliente);
    CuentaCorrienteCliente cuentaCorrienteCliente = new CuentaCorrienteCliente();
    cuentaCorrienteCliente.setCliente(cliente);
    cuentaCorrienteCliente.setEmpresa(cliente.getEmpresa());
    cuentaCorrienteCliente.setFechaApertura(cliente.getFechaAlta());
    if (cliente.getCredencial() != null) {
      Cliente clienteYaAsignado =
          this.getClientePorIdUsuarioYidEmpresa(
              cliente.getCredencial().getId_Usuario(), cliente.getEmpresa().getId_Empresa());
      if (clienteYaAsignado != null) {
        throw new BusinessServiceException(
            MessageFormat.format(
                ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_cliente_credencial_no_valida"),
                clienteYaAsignado.getRazonSocial()));
      }
    }
    cliente = clienteRepository.save(cliente);
    cuentaCorrienteService.guardarCuentaCorrienteCliente(cuentaCorrienteCliente);
    logger.warn("El Cliente " + cliente + " se guardó correctamente.");
    return cliente;
  }

  @Override
  @Transactional
  public void actualizar(Cliente clientePorActualizar, Cliente clientePersistido) {
    clientePorActualizar.setFechaAlta(clientePersistido.getFechaAlta());
    clientePorActualizar.setPredeterminado(clientePersistido.isPredeterminado());
    clientePorActualizar.setEliminado(clientePersistido.isEliminado());
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
                ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_cliente_credencial_no_valida"),
                clienteYaAsignado.getRazonSocial()));
      }
    }
    clienteRepository.save(clientePorActualizar);
    logger.warn("El Cliente " + clientePorActualizar + " se actualizó correctamente.");
  }

  @Override
  @Transactional
  public void eliminar(long idCliente) {
    Cliente cliente = this.getClientePorId(idCliente);
    if (cliente == null) {
      throw new EntityNotFoundException(
          ResourceBundle.getBundle("Mensajes").getString("mensaje_cliente_no_existente"));
    }
    cliente.setEliminado(true);
    clienteRepository.save(cliente);
    logger.warn("El Cliente " + cliente + " se eliminó correctamente.");
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
  public String calcularNroDeCliente(Empresa empresa) {
    long min = 1L;
    long max = 99999L; // 5 digitos
    long randomLong = 0L;
    boolean esRepetido = true;
    while (esRepetido) {
      randomLong = min + (long) (Math.random() * (max - min));
      String strinLong = Long.toString(randomLong);
      Cliente c = clienteRepository.findByNroClienteAndEmpresaAndEliminado(strinLong, empresa, false);
      if (c == null) esRepetido = false;
    }
    return Long.toString(randomLong);
  }

}
