package sic.service.impl;

import com.querydsl.core.BooleanBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javax.persistence.EntityNotFoundException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    public List<Cliente> getClientes(Empresa empresa, long idUsuario) {
        Pageable pageable = new PageRequest(0, Integer.MAX_VALUE, new Sort(Sort.Direction.ASC, "razonSocial"));
        BusquedaClienteCriteria criteria = BusquedaClienteCriteria.builder()
                                                                  .empresa(empresa)
                                                                  .pageable(pageable)
                                                                  .build();
        return this.buscarClientes(criteria, idUsuario).getContent();
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
        Cliente cliente = clienteRepository.findByAndEmpresaAndPredeterminadoAndEliminado(empresa, true, false); 
        if (cliente == null) {
            throw new EntityNotFoundException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_cliente_sin_predeterminado"));
        }
        return cliente;                   
    }
    
    @Override
    public boolean existeClientePredeterminado(Empresa empresa) {
        return clienteRepository.existsByAndEmpresaAndPredeterminadoAndEliminado(empresa, true, false);
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
        Cliente clientePredeterminadoAnterior = clienteRepository.findByAndEmpresaAndPredeterminadoAndEliminado(cliente.getEmpresa(), true, false);
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
            throw new EntityNotFoundException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_empresa_no_existente"));
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
        if (criteria.isBuscaPorProvincia()) builder.and(qcliente.localidad.provincia.eq(criteria.getProvincia()));
        if (criteria.isBuscaPorPais()) builder.and(qcliente.localidad.provincia.pais.eq(criteria.getPais()));
        Usuario usuarioLoggedIn = usuarioService.getUsuarioPorId(idUsuarioLoggedIn);
        if (usuarioLoggedIn.getRoles().contains(Rol.VIAJANTE) && usuarioLoggedIn.getRoles().contains(Rol.CLIENTE)) {
            builder.and(qcliente.viajante.eq(usuarioLoggedIn).or(qcliente.eq(this.getClientePorIdUsuario(usuarioLoggedIn.getId_Usuario()))));
        }
        if (usuarioLoggedIn.getRoles().contains(Rol.VIAJANTE)) builder.and(qcliente.viajante.eq(usuarioLoggedIn));
        if (usuarioLoggedIn.getRoles().contains(Rol.CLIENTE)) builder.and(qcliente.eq(this.getClientePorIdUsuario(usuarioLoggedIn.getId_Usuario())));
        builder.and(qcliente.empresa.eq(criteria.getEmpresa()).and(qcliente.eliminado.eq(false)));
        Page<Cliente> page = clienteRepository.findAll(builder, criteria.getPageable());
        page.getContent().forEach(c -> {
            CuentaCorriente cc = cuentaCorrienteService.getCuentaCorrientePorCliente(c);
            c.setSaldoCuentaCorriente(cc.getSaldo());
            c.setFechaUltimoMovimiento(cc.getFechaUltimoMovimiento());
        });
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
    public Cliente guardar(Cliente cliente) {        
        this.validarOperacion(TipoDeOperacion.ALTA, cliente);
        cliente = clienteRepository.save(cliente);  
        CuentaCorrienteCliente cuentaCorrienteCliente = new CuentaCorrienteCliente();
        cuentaCorrienteCliente.setCliente(cliente);
        cuentaCorrienteCliente.setEmpresa(cliente.getEmpresa());
        cuentaCorrienteCliente.setFechaApertura(cliente.getFechaAlta());
        cuentaCorrienteService.guardarCuentaCorrienteCliente(cuentaCorrienteCliente);
        LOGGER.warn("El Cliente " + cliente + " se guardó correctamente." );
        return cliente;
    }

    @Override
    @Transactional
    public void actualizar(Cliente cliente) {
        this.validarOperacion(TipoDeOperacion.ACTUALIZACION, cliente);        
        clienteRepository.save(cliente);                   
    }

    @Override
    @Transactional
    public void eliminar(long idCliente) {
        Cliente cliente = this.getClientePorId(idCliente);
        if (cliente == null) {
            throw new EntityNotFoundException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_cliente_no_existente"));
        }
        cliente.setCredencial(null);
        cliente.setEliminado(true);        
        clienteRepository.save(cliente);                   
    }
    
    @Override
    public Cliente getClientePorIdPedido(long idPedido) {
        return clienteRepository.findClienteByIdPedido(idPedido);
    }

    @Override
    public Cliente getClientePorIdUsuario(long idUsuario) {
        return clienteRepository.findClienteByIdUsuario(idUsuario);
    }
}
