package sic.service.impl;

import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;
import javax.persistence.EntityNotFoundException;

import com.querydsl.core.BooleanBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sic.modelo.*;
import sic.service.IClienteService;
import sic.service.IUsuarioService;
import sic.service.BusinessServiceException;
import sic.util.Utilidades;
import sic.util.Validator;
import sic.repository.UsuarioRepository;
import sic.service.IEmpresaService;

@Service
@Transactional
public class UsuarioServiceImpl implements IUsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final IEmpresaService empresaService;
    private final IClienteService clienteService;
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    @Lazy
    public UsuarioServiceImpl(UsuarioRepository usuarioRepository, IEmpresaService empresaService,
                              IClienteService clienteService) {
        this.usuarioRepository = usuarioRepository;
        this.empresaService = empresaService;
        this.clienteService = clienteService;
    }

    @Override
    public Usuario getUsuarioPorId(Long idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario);
        if (usuario == null) {
            throw new EntityNotFoundException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_usuario_no_existente"));
        }
        return usuario;
    }
        
    @Override
    public Usuario autenticarUsuario(Credencial credencial) {
        Usuario usuario = usuarioRepository.findByUsernameOrEmailAndPasswordAndEliminado(credencial.getUsername(),
                credencial.getUsername(), Utilidades.encriptarConMD5(credencial.getPassword()));
        if (usuario == null) {
            throw new EntityNotFoundException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_usuario_no_existente"));
        }
        return usuario;
    }

  @Override
  public Page<Usuario> buscarUsuarios(BusquedaUsuarioCriteria criteria, long idUsuarioLoggedIn) {
    Usuario usuarioLoggedIn = this.getUsuarioPorId(idUsuarioLoggedIn);
    if (usuarioLoggedIn.getRoles().contains(Rol.ADMINISTRADOR)) {
      if (criteria.getEmpresa() == null) {
        throw new EntityNotFoundException(
            ResourceBundle.getBundle("Mensajes").getString("mensaje_empresa_no_existente"));
      }
      QUsuario qusuario = QUsuario.usuario;
      BooleanBuilder builder = new BooleanBuilder();
      if (criteria.isBuscaPorApellido()) {
        String[] terminos = criteria.getApellido().split(" ");
        BooleanBuilder rsPredicate = new BooleanBuilder();
        for (String termino : terminos) {
          rsPredicate.and(qusuario.apellido.containsIgnoreCase(termino));
        }
        builder.or(rsPredicate);
      }
      if (criteria.isBuscaPorNombre()) {
        String[] terminos = criteria.getNombre().split(" ");
        BooleanBuilder rsPredicate = new BooleanBuilder();
        for (String termino : terminos) {
          rsPredicate.and(qusuario.nombre.containsIgnoreCase(termino));
        }
        builder.or(rsPredicate);
      }
      if (criteria.isBuscarPorNombreDeUsuario()) {
        String[] terminos = criteria.getUsername().split(" ");
        BooleanBuilder rsPredicate = new BooleanBuilder();
        for (String termino : terminos) {
          rsPredicate.and(qusuario.username.containsIgnoreCase(termino));
        }
        builder.or(rsPredicate);
      }
      if (criteria.isBuscaPorEmail()) {
        String[] terminos = criteria.getEmail().split(" ");
        BooleanBuilder rsPredicate = new BooleanBuilder();
        for (String termino : terminos) {
          rsPredicate.and(qusuario.email.containsIgnoreCase(termino));
        }
        builder.or(rsPredicate);
      }
      if (criteria.isBuscarPorRol() && !criteria.getRoles().isEmpty()) {
        BooleanBuilder rsPredicate = new BooleanBuilder();
        for (Rol rol : criteria.getRoles()) {
          switch (rol) {
            case ADMINISTRADOR:
              rsPredicate.or(qusuario.roles.contains(Rol.ADMINISTRADOR));
              break;
            case VENDEDOR:
              rsPredicate.or(qusuario.roles.contains(Rol.VENDEDOR));
              break;
            case VIAJANTE:
              rsPredicate.or(qusuario.roles.contains(Rol.VIAJANTE));
              break;
            case CLIENTE:
              rsPredicate.or(qusuario.roles.contains(Rol.CLIENTE));
              break;
          }
        }
        builder.and(rsPredicate);
      }
      builder.and(qusuario.eliminado.eq(false));
      return usuarioRepository.findAll(builder, criteria.getPageable());
    } else return null;
  }

    private void validarOperacion(TipoDeOperacion operacion, Usuario usuario) {
        // Requeridos
        if (Validator.esVacio(usuario.getNombre())) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_usuario_vacio_nombre"));
        }
        if (Validator.esVacio(usuario.getApellido())) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_usuario_vacio_apellido"));
        }
        if (Validator.esVacio(usuario.getUsername())) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_usuario_vacio_username"));
        }
        if (!Validator.esEmailValido(usuario.getEmail())) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_usuario_invalido_email"));
        }
        if (usuario.getRoles().isEmpty()) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_usuario_no_selecciono_rol"));
        }
        if (operacion == TipoDeOperacion.ALTA) {
            if (Validator.esVacio(usuario.getPassword())) {
                throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_usuario_vacio_password"));
            }
        }
        // Username sin espacios en blanco
        if (usuario.getUsername().contains(" ")) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_usuario_username_con_espacios"));
        }
        // Duplicados
        if (operacion == TipoDeOperacion.ALTA) {
            // username
            if (usuarioRepository.findByUsernameAndEliminado(usuario.getUsername(), false) != null) {
                throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_usuario_duplicado_username"));
            }
            // email
            if (usuarioRepository.findByEmailAndEliminado(usuario.getEmail(), false) != null) {
                throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_usuario_duplicado_email"));
            }
        }
        if (operacion == TipoDeOperacion.ACTUALIZACION) {
            // username
            Usuario usuarioGuardado = usuarioRepository.findByUsernameAndEliminado(usuario.getUsername(), false);
            if (usuarioGuardado != null && usuarioGuardado.getId_Usuario() != usuario.getId_Usuario()) {
                throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_usuario_duplicado_username"));
            }
            // email
            usuarioGuardado = usuarioRepository.findByEmailAndEliminado(usuario.getEmail(), false);
            if (usuarioGuardado != null && usuarioGuardado.getId_Usuario() != usuario.getId_Usuario()) {
                throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_usuario_duplicado_email"));
            }
        }
        // Ultimo usuario administrador
        if ((operacion == TipoDeOperacion.ACTUALIZACION && !usuario.getRoles().contains(Rol.ADMINISTRADOR))
                || operacion == TipoDeOperacion.ELIMINACION && usuario.getRoles().contains(Rol.ADMINISTRADOR)) {
            Pageable pageable = new PageRequest(0, 1, new Sort(Sort.Direction.DESC, "id_Usuario"));
            QUsuario qusuario = QUsuario.usuario;
            BooleanBuilder builder = new BooleanBuilder();
            builder.and(qusuario.roles.contains(Rol.ADMINISTRADOR)).and(qusuario.eliminado.eq(false));
            List<Usuario> adminitradores = usuarioRepository.findAll(builder, pageable).getContent();
            if (adminitradores.size() == 1) {
                if (adminitradores.get(0).getId_Usuario() == usuario.getId_Usuario()) {
                    throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                            .getString("mensaje_usuario_ultimoAdmin"));
                }
            }
        }
    }

    @Override
    public void actualizar(Usuario usuario, Long idCliente) {
        this.validarOperacion(TipoDeOperacion.ACTUALIZACION, usuario);
        if (usuario.getPassword().isEmpty()) {
            Usuario usuarioGuardado = usuarioRepository.findById(usuario.getId_Usuario());
            usuario.setPassword(usuarioGuardado.getPassword());
        } else {
            usuario.setPassword(Utilidades.encriptarConMD5(usuario.getPassword()));
        }
        Cliente cliente;
        if (usuario.getRoles().contains(Rol.CLIENTE) && idCliente != null) {
            this.actualizarCredencial(idCliente, usuario);
        } else if(!usuario.getRoles().contains(Rol.CLIENTE)) {
            cliente = clienteService.getClientePorIdUsuario(usuario.getId_Usuario());
            if (cliente != null) {
                cliente.setCredencial(null);
                clienteService.actualizar(cliente);
            }
        }
        usuarioRepository.save(usuario);
    }
    
    @Override
    public void actualizarToken(String token, long idUsuario) {
        usuarioRepository.updateToken(token, idUsuario);
    }

    @Override
    public Usuario guardar(Usuario usuario, Long idCliente) {
        if (usuario.getRoles().contains(Rol.CLIENTE)) {
            this.actualizarCredencial(idCliente, usuario);
        }
        this.validarOperacion(TipoDeOperacion.ALTA, usuario);
        usuario.setPassword(Utilidades.encriptarConMD5(usuario.getPassword()));
        usuario = usuarioRepository.save(usuario);
        LOGGER.warn("El Usuario " + usuario + " se guardó correctamente.");
        return usuario;
    }

    private void actualizarCredencial(long idCliente, Usuario usuario) {
        Cliente cliente = clienteService.getClientePorId(idCliente);
        if (cliente.getCredencial() == null) {
            cliente.setCredencial(usuario);
            clienteService.actualizar(cliente);
        } else {
            String mensaje = ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_usuario_cliente_no_valido");
            throw new BusinessServiceException(MessageFormat.format(mensaje, cliente.getCredencial().getUsername()));
        }
    }

    @Override
    public void eliminar(long idUsuario) {
        Usuario usuario = this.getUsuarioPorId(idUsuario);
        if (usuario == null) {
            throw new EntityNotFoundException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_usuario_no_existente"));
        }
        this.validarOperacion(TipoDeOperacion.ELIMINACION, usuario);
        usuario.setEliminado(true);
        usuarioRepository.save(usuario);
    }
    
    @Override
    public int actualizarIdEmpresaDeUsuario(long idUsuario, long idEmpresaPredeterminada) {
        if (empresaService.getEmpresaPorId(idEmpresaPredeterminada) == null) {
            throw new EntityNotFoundException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_empresa_no_existente"));
        }
        return usuarioRepository.updateIdEmpresa(idUsuario, idEmpresaPredeterminada);
    }

}
