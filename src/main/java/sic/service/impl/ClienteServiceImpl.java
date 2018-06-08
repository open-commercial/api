package sic.service.impl;

import com.querydsl.core.BooleanBuilder;

import java.text.MessageFormat;
import java.util.List;
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
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public ClienteServiceImpl(ClienteRepository clienteRepository, ICuentaCorrienteService cuentaCorrienteService,
                              IUsuarioService usuarioService) {
        this.clienteRepository = clienteRepository;
        this.cuentaCorrienteService = cuentaCorrienteService;
        this.usuarioService = usuarioService;
    }

    @Override
    public Cliente getClientePorId(long idCliente) {
        Cliente cliente = clienteRepository.findOne(idCliente);
        if (cliente == null) {
            throw new EntityNotFoundException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_cliente_no_existente"));
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
        Cliente cliente = clienteRepository.findByEmpresaAndPredeterminadoAndEliminado(empresa, true, false);
        if (cliente == null) {
            throw new EntityNotFoundException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_cliente_sin_predeterminado"));
        }
        return cliente;
    }

    @Override
    public boolean existeClientePredeterminado(Empresa empresa) {
        return clienteRepository.existsByEmpresaAndPredeterminadoAndEliminado(empresa, true, false);
    }

    /**
     * Establece el @cliente pasado como parametro como predeterminado. Antes de
     * establecer el cliente como predeterminado, verifica si ya existe otro como
     * predeterminado y cambia su estado.
     *
     * @param cliente Cliente candidato a predeterminado.
     */
    @Override
    @Transactional
    public void setClientePredeterminado(Cliente cliente) {
        Cliente clientePredeterminadoAnterior = clienteRepository.findByEmpresaAndPredeterminadoAndEliminado(cliente.getEmpresa(), true, false);
        if (clientePredeterminadoAnterior != null) {
            clientePredeterminadoAnterior.setPredeterminado(false);
            clienteRepository.save(clientePredeterminadoAnterior);
        }
        cliente.setPredeterminado(true);
        clienteRepository.save(cliente);
    }

  @Override
  public Page<Cliente> buscarClientes(BusquedaClienteCriteria criteria, long idUsuarioLoggedIn) {
    if (criteria.getEmpresa() == null) {
      throw new EntityNotFoundException(
          ResourceBundle.getBundle("Mensajes").getString("mensaje_empresa_no_existente"));
    }
    QCliente qcliente = QCliente.cliente;
    BooleanBuilder builder = new BooleanBuilder();
    if (criteria.isBuscaPorRazonSocial()) {
      String[] terminos = criteria.getRazonSocial().split(" ");
      BooleanBuilder rsPredicate = new BooleanBuilder();
      for (String termino : terminos) {
        rsPredicate.and(qcliente.razonSocial.containsIgnoreCase(termino));
      }
      builder.or(rsPredicate);
    }
    if (criteria.isBuscaPorNombreFantasia()) {
      String[] terminos = criteria.getNombreFantasia().split(" ");
      BooleanBuilder nfPredicate = new BooleanBuilder();
      for (String termino : terminos) {
        nfPredicate.and(qcliente.nombreFantasia.containsIgnoreCase(termino));
      }
      builder.or(nfPredicate);
    }
    if (criteria.isBuscaPorId_Fiscal()) {
      String[] terminos = criteria.getIdFiscal().split(" ");
      BooleanBuilder idPredicate = new BooleanBuilder();
      for (String termino : terminos) {
        idPredicate.and(qcliente.idFiscal.containsIgnoreCase(termino));
      }
      builder.or(idPredicate);
    }
    if (criteria.isBuscaPorViajante()) builder.and(qcliente.viajante.eq(criteria.getViajante()));
    if (criteria.isBuscaPorLocalidad()) builder.and(qcliente.localidad.eq(criteria.getLocalidad()));
    if (criteria.isBuscaPorProvincia())
      builder.and(qcliente.localidad.provincia.eq(criteria.getProvincia()));
    if (criteria.isBuscaPorPais())
      builder.and(qcliente.localidad.provincia.pais.eq(criteria.getPais()));
    Usuario usuarioLoggedIn = usuarioService.getUsuarioPorId(idUsuarioLoggedIn);
    if (!usuarioLoggedIn.getRoles().contains(Rol.ADMINISTRADOR)
        && !usuarioLoggedIn.getRoles().contains(Rol.VENDEDOR)) {
      if (usuarioLoggedIn.getRoles().contains(Rol.VIAJANTE)
          && usuarioLoggedIn.getRoles().contains(Rol.CLIENTE)) {
        builder.and(
            qcliente
                .viajante
                .eq(usuarioLoggedIn)
                .or(
                    qcliente.eq(
                        this.getClientePorIdUsuarioYidEmpresa(
                            usuarioLoggedIn.getId_Usuario(),
                            criteria.getEmpresa()))));
      } else {
        if (usuarioLoggedIn.getRoles().contains(Rol.VIAJANTE))
          builder.and(qcliente.viajante.eq(usuarioLoggedIn));
        if (usuarioLoggedIn.getRoles().contains(Rol.CLIENTE))
          builder.and(
              qcliente.eq(
                  this.getClientePorIdUsuarioYidEmpresa(
                      usuarioLoggedIn.getId_Usuario(), criteria.getEmpresa())));
      }
    }
    builder.and(qcliente.empresa.eq(criteria.getEmpresa()).and(qcliente.eliminado.eq(false)));
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
        //Entrada de Datos
        if (cliente.getEmail() != null && !cliente.getEmail().equals("")) {
            if (!Validator.esEmailValido(cliente.getEmail())) {
                throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_cliente_email_invalido"));
            }
        }
        //Requeridos
        if (Validator.esVacio(cliente.getRazonSocial())) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_cliente_vacio_razonSocial"));
        }
        if (cliente.getCondicionIVA() == null) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_cliente_vacio_condicionIVA"));
        }
        if (cliente.getLocalidad() == null) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_cliente_vacio_localidad"));
        }
        if (cliente.getEmpresa() == null) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_cliente_vacio_empresa"));
        }
        //Duplicados
        //ID Fiscal
        if (!cliente.getIdFiscal().equals("")) {
            Cliente clienteDuplicado = this.getClientePorIdFiscal(cliente.getIdFiscal(), cliente.getEmpresa());
            if (operacion.equals(TipoDeOperacion.ACTUALIZACION)
                    && clienteDuplicado != null
                    && clienteDuplicado.getId_Cliente() != cliente.getId_Cliente()) {
                throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_cliente_duplicado_idFiscal"));
            }
            if (operacion.equals(TipoDeOperacion.ALTA)
                    && clienteDuplicado != null
                    && !cliente.getIdFiscal().equals("")) {
                throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_cliente_duplicado_idFiscal"));
            }
        }
        //Razon Social
        Cliente clienteDuplicado = this.getClientePorRazonSocial(cliente.getRazonSocial(), cliente.getEmpresa());
        if (operacion.equals(TipoDeOperacion.ALTA) && clienteDuplicado != null) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_cliente_duplicado_razonSocial"));
        }
        if (operacion.equals(TipoDeOperacion.ACTUALIZACION)) {
            if (clienteDuplicado != null && clienteDuplicado.getId_Cliente() != cliente.getId_Cliente()) {
                throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_cliente_duplicado_razonSocial"));
            }
        }
    }

  @Override
  @Transactional
  public Cliente guardar(Cliente cliente, Long idUsuarioCrendencial, long idUsuarioLoggedIn) {
    usuarioService.verificarAdministrador(idUsuarioLoggedIn);
    this.validarOperacion(TipoDeOperacion.ALTA, cliente);
    CuentaCorrienteCliente cuentaCorrienteCliente = new CuentaCorrienteCliente();
    cuentaCorrienteCliente.setCliente(cliente);
    cuentaCorrienteCliente.setEmpresa(cliente.getEmpresa());
    cuentaCorrienteCliente.setFechaApertura(cliente.getFechaAlta());
    if (idUsuarioCrendencial != null) {
      Cliente clienteYaAsignado =
          this.getClientePorIdUsuarioYidEmpresa(idUsuarioCrendencial, cliente.getEmpresa());
      if (clienteYaAsignado != null) {
        throw new BusinessServiceException(
            MessageFormat.format(
                ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_cliente_credencial_no_valida"),
                clienteYaAsignado.getRazonSocial()));
      }
      cliente.setCredencial(usuarioService.getUsuarioPorId(idUsuarioCrendencial));
      this.editarRolUsuario(cliente, idUsuarioLoggedIn, true);
    }
    cliente = clienteRepository.save(cliente);
    cuentaCorrienteService.guardarCuentaCorrienteCliente(cuentaCorrienteCliente);
    LOGGER.warn("El Cliente " + cliente + " se guardó correctamente.");
    return cliente;
  }

  @Override
  @Transactional
  public void actualizar(Cliente cliente, Long idUsuarioCrendencial, long idUsuarioLoggedIn) {
    usuarioService.verificarAdministrador(idUsuarioLoggedIn);
    this.validarOperacion(TipoDeOperacion.ACTUALIZACION, cliente);
    if (idUsuarioCrendencial != null) {
      Cliente clienteYaAsignado =
          this.getClientePorIdUsuarioYidEmpresa(idUsuarioCrendencial, cliente.getEmpresa());
      if (clienteYaAsignado != null) {
        throw new BusinessServiceException(
            MessageFormat.format(
                ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_cliente_credencial_no_valida"),
                clienteYaAsignado.getRazonSocial()));
      }
      cliente.setCredencial(usuarioService.getUsuarioPorId(idUsuarioCrendencial));
      this.editarRolUsuario(cliente, idUsuarioLoggedIn, true);
    } else {
      this.editarRolUsuario(cliente, idUsuarioLoggedIn, false);
      cliente.setCredencial(null);
    }
    clienteRepository.save(cliente);
  }

  private void editarRolUsuario(Cliente cliente, long idUsuarioLoggedIn, boolean agregar) {
    Usuario usuarioAModificarRol =
        usuarioService.getUsuarioPorId(cliente.getCredencial().getId_Usuario());
    List<Rol> roles = usuarioAModificarRol.getRoles();
    if (agregar) {
      if (!roles.contains(Rol.CLIENTE)) {
        roles.add(Rol.CLIENTE);
      }
    } else {
      if (this.getClientesPorIdUsuario(cliente.getCredencial().getId_Usuario()).size() == 1) {
        roles.remove(Rol.CLIENTE);
      }
    }
    usuarioAModificarRol.setRoles(roles);
    usuarioService.actualizar(usuarioAModificarRol, idUsuarioLoggedIn);
  }

    @Override
    @Transactional
    public void eliminar(long idCliente, long idUsuarioLoggedIn) {
        usuarioService.verificarAdministrador(idUsuarioLoggedIn);
        Cliente cliente = this.getClientePorId(idCliente);
        if (cliente == null) {
            throw new EntityNotFoundException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_cliente_no_existente"));
        }
        cliente.setEliminado(true);
        clienteRepository.save(cliente);
    }

    @Override
    public Cliente getClientePorIdPedido(long idPedido) {
        return clienteRepository.findClienteByIdPedido(idPedido);
    }

    @Override
    public Cliente getClientePorIdUsuarioYidEmpresa(long idUsuario, Empresa empresa) {
        return clienteRepository.findClienteByIdUsuarioYidEmpresa(idUsuario, empresa.getId_Empresa());
    }

    @Override
    public List<Cliente> getClientesPorIdUsuario(long idUsuario) {
        return clienteRepository.findClienteByIdUsuario(idUsuario);
    }

}
