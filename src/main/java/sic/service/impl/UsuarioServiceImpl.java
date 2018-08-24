package sic.service.impl;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import javax.persistence.EntityNotFoundException;
import com.querydsl.core.BooleanBuilder;
import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sic.controller.UnauthorizedException;
import sic.modelo.*;
import sic.service.*;
import sic.util.Validator;
import sic.repository.UsuarioRepository;

@Service
@Transactional
public class UsuarioServiceImpl implements IUsuarioService {

  private final UsuarioRepository usuarioRepository;
  private final IEmpresaService empresaService;
  private final IClienteService clienteService;
  private final ICorreoElectronicoService correoElectronicoService;
  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Autowired
  @Lazy
  public UsuarioServiceImpl(
      UsuarioRepository usuarioRepository,
      IEmpresaService empresaService,
      IClienteService clienteService,
      ICorreoElectronicoService correoElectronicoService) {
    this.usuarioRepository = usuarioRepository;
    this.empresaService = empresaService;
    this.clienteService = clienteService;
    this.correoElectronicoService = correoElectronicoService;
  }

  public String encriptarConMD5(String password) {
    try {
      MessageDigest md = MessageDigest.getInstance("MD5");
      byte[] array = md.digest(password.getBytes());
      StringBuilder sb = new StringBuilder();
      for (byte anArray : array) {
        sb.append(Integer.toHexString((anArray & 0xFF) | 0x100).substring(1, 3));
      }
      return sb.toString();
    } catch (NoSuchAlgorithmException ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public Usuario getUsuarioPorId(Long idUsuario) {
    Usuario usuario = usuarioRepository.findById(idUsuario);
    if (usuario == null) {
      throw new EntityNotFoundException(
          ResourceBundle.getBundle("Mensajes").getString("mensaje_usuario_no_existente"));
    }
    return usuario;
  }

  @Override
  public Usuario getUsuarioPorPasswordRecoveryKeyAndIdUsuario(String passwordRecoveryKey, long idUsuario) {
      return usuarioRepository.findByPasswordRecoveryKeyAndIdUsuarioAndEliminadoAndHabilitado(passwordRecoveryKey, idUsuario);
  }

  @Override
  public Usuario autenticarUsuario(Credencial credencial) {
    Usuario usuario =
        usuarioRepository.findByUsernameOrEmailAndPasswordAndEliminado(
            credencial.getUsername(),
            credencial.getUsername(),
            this.encriptarConMD5(credencial.getPassword()));
    if (usuario == null) {
      throw new UnauthorizedException(
          ResourceBundle.getBundle("Mensajes").getString("mensaje_usuario_logInInvalido"));
    }
    if (!usuario.isHabilitado()) {
      throw new UnauthorizedException(
          ResourceBundle.getBundle("Mensajes").getString("mensaje_usuario_no_habilitado"));
    }
    return usuario;
  }

  @Override
  public Page<Usuario> buscarUsuarios(BusquedaUsuarioCriteria criteria) {
    QUsuario qUsuario = QUsuario.usuario;
    BooleanBuilder builder = new BooleanBuilder();
    if (criteria.isBuscaPorApellido()) {
      String[] terminos = criteria.getApellido().split(" ");
      BooleanBuilder rsPredicate = new BooleanBuilder();
      for (String termino : terminos) {
        rsPredicate.and(qUsuario.apellido.containsIgnoreCase(termino));
      }
      builder.or(rsPredicate);
    }
    if (criteria.isBuscaPorNombre()) {
      String[] terminos = criteria.getNombre().split(" ");
      BooleanBuilder rsPredicate = new BooleanBuilder();
      for (String termino : terminos) {
        rsPredicate.and(qUsuario.nombre.containsIgnoreCase(termino));
      }
      builder.or(rsPredicate);
    }
    if (criteria.isBuscarPorNombreDeUsuario()) {
      String[] terminos = criteria.getUsername().split(" ");
      BooleanBuilder rsPredicate = new BooleanBuilder();
      for (String termino : terminos) {
        rsPredicate.and(qUsuario.username.containsIgnoreCase(termino));
      }
      builder.or(rsPredicate);
    }
    if (criteria.isBuscaPorEmail()) {
      String[] terminos = criteria.getEmail().split(" ");
      BooleanBuilder rsPredicate = new BooleanBuilder();
      for (String termino : terminos) {
        rsPredicate.and(qUsuario.email.containsIgnoreCase(termino));
      }
      builder.or(rsPredicate);
    }
    if (criteria.isBuscarPorRol() && !criteria.getRoles().isEmpty()) {
      BooleanBuilder rsPredicate = new BooleanBuilder();
      for (Rol rol : criteria.getRoles()) {
        switch (rol) {
          case ADMINISTRADOR:
            rsPredicate.or(qUsuario.roles.contains(Rol.ADMINISTRADOR));
            break;
          case ENCARGADO:
            rsPredicate.or(qUsuario.roles.contains(Rol.ENCARGADO));
            break;
          case VENDEDOR:
            rsPredicate.or(qUsuario.roles.contains(Rol.VENDEDOR));
            break;
          case VIAJANTE:
            rsPredicate.or(qUsuario.roles.contains(Rol.VIAJANTE));
            break;
          case COMPRADOR:
            rsPredicate.or(qUsuario.roles.contains(Rol.COMPRADOR));
            break;
        }
      }
      builder.and(rsPredicate);
    }
    builder.and(qUsuario.eliminado.eq(false));
    return usuarioRepository.findAll(builder, criteria.getPageable());
  }

  private void validarOperacion(TipoDeOperacion operacion, Usuario usuario) {
    // Requeridos
    if (Validator.esVacio(usuario.getNombre())) {
      throw new BusinessServiceException(
          ResourceBundle.getBundle("Mensajes").getString("mensaje_usuario_vacio_nombre"));
    }
    if (Validator.esVacio(usuario.getApellido())) {
      throw new BusinessServiceException(
          ResourceBundle.getBundle("Mensajes").getString("mensaje_usuario_vacio_apellido"));
    }
    if (Validator.esVacio(usuario.getUsername())) {
      throw new BusinessServiceException(
          ResourceBundle.getBundle("Mensajes").getString("mensaje_usuario_vacio_username"));
    }
    if (!Validator.esEmailValido(usuario.getEmail())) {
      throw new BusinessServiceException(
          ResourceBundle.getBundle("Mensajes").getString("mensaje_usuario_invalido_email"));
    }
    if (operacion == TipoDeOperacion.ALTA) {
      if (Validator.esVacio(usuario.getPassword())) {
        throw new BusinessServiceException(
            ResourceBundle.getBundle("Mensajes").getString("mensaje_usuario_vacio_password"));
      }
    }
    if (usuario.getRoles().isEmpty()) {
      throw new BusinessServiceException(
          ResourceBundle.getBundle("Mensajes").getString("mensaje_usuario_no_selecciono_rol"));
    }
    // Username sin espacios en blanco
    if (usuario.getUsername().contains(" ")) {
      throw new BusinessServiceException(
          ResourceBundle.getBundle("Mensajes").getString("mensaje_usuario_username_con_espacios"));
    }
    // Duplicados
    if (operacion == TipoDeOperacion.ALTA) {
      // username
      if (usuarioRepository.findByUsernameAndEliminado(usuario.getUsername(), false) != null) {
        throw new BusinessServiceException(
            ResourceBundle.getBundle("Mensajes").getString("mensaje_usuario_duplicado_username"));
      }
      // email
      if (usuarioRepository.findByEmailAndEliminadoAndHabilitado(usuario.getEmail(), false, true) != null) {
        throw new BusinessServiceException(
            ResourceBundle.getBundle("Mensajes").getString("mensaje_usuario_duplicado_email"));
      }
    }
    if (operacion == TipoDeOperacion.ACTUALIZACION) {
      // username
      Usuario usuarioGuardado =
          usuarioRepository.findByUsernameAndEliminado(usuario.getUsername(), false);
      if (usuarioGuardado != null && usuarioGuardado.getId_Usuario() != usuario.getId_Usuario()) {
        throw new BusinessServiceException(
            ResourceBundle.getBundle("Mensajes").getString("mensaje_usuario_duplicado_username"));
      }
      // email
      usuarioGuardado = usuarioRepository.findByEmailAndEliminadoAndHabilitado(usuario.getEmail(), false, true);
      if (usuarioGuardado != null && usuarioGuardado.getId_Usuario() != usuario.getId_Usuario()) {
        throw new BusinessServiceException(
            ResourceBundle.getBundle("Mensajes").getString("mensaje_usuario_duplicado_email"));
      }
    }
    // Ultimo usuario administrador
    if ((operacion == TipoDeOperacion.ACTUALIZACION
            && !usuario.getRoles().contains(Rol.ADMINISTRADOR))
        || operacion == TipoDeOperacion.ELIMINACION
            && usuario.getRoles().contains(Rol.ADMINISTRADOR)) {
      List<Usuario> administradores = this.getUsuariosPorRol(Rol.ADMINISTRADOR).getContent();
      if (administradores.size() == 1
          && administradores.get(0).getId_Usuario() == usuario.getId_Usuario()) {
        throw new BusinessServiceException(
            ResourceBundle.getBundle("Mensajes").getString("mensaje_usuario_ultimoAdmin"));
      }
    }
  }

  @Override
  public Page<Usuario> getUsuariosPorRol(Rol rol) {
    Pageable pageable = new PageRequest(0, Integer.MAX_VALUE);
    return usuarioRepository.findAllByRolesContainsAndEliminado(rol, false, pageable);
  }

  @Override
  public void actualizar(Usuario usuario, Usuario usuarioLoggedIn) {
      this.validarOperacion(TipoDeOperacion.ACTUALIZACION, usuario);
      if (!usuarioLoggedIn.getRoles().contains(Rol.ADMINISTRADOR)) {
        usuario.setRoles(usuarioLoggedIn.getRoles());
      }
      if (usuario.getPassword().isEmpty()) {
        Usuario usuarioGuardado = usuarioRepository.findById(usuario.getId_Usuario());
        usuario.setPassword(usuarioGuardado.getPassword());
      } else {
        usuario.setPassword(this.encriptarConMD5(usuario.getPassword()));
      }
      if (!usuario.getRoles().contains(Rol.VIAJANTE)) {
        this.clienteService.desvincularClienteDeViajante(usuario.getId_Usuario());
      }
      if (!usuario.getRoles().contains(Rol.COMPRADOR)) {
        this.clienteService.desvincularClienteDeCredencial(usuario.getId_Usuario());
      }
      if (usuarioLoggedIn.getId_Usuario() == usuario.getId_Usuario()) {
        usuario.setToken(usuarioLoggedIn.getToken());
      }
      usuarioRepository.save(usuario);
      logger.warn("El Usuario " + usuario + " se actualizó correctamente.");
  }

  @Override
  public void actualizarToken(String token, long idUsuario) {
    usuarioRepository.updateToken(token, idUsuario);
  }

  @Override
  public void actualizarPasswordRecoveryKey(String passwordRecoveryKey, long idUsuario) {
    usuarioRepository.updatePasswordRecoveryKey(passwordRecoveryKey,
            Date.from(LocalDateTime.now().plusHours(3L).atZone(ZoneId.systemDefault()).toInstant()), idUsuario);
  }

  @Override
  public int actualizarIdEmpresaDeUsuario(long idUsuario, long idEmpresaPredeterminada) {
    if (empresaService.getEmpresaPorId(idEmpresaPredeterminada) == null) {
      throw new EntityNotFoundException(
              ResourceBundle.getBundle("Mensajes").getString("mensaje_empresa_no_existente"));
    }
    return usuarioRepository.updateIdEmpresa(idUsuario, idEmpresaPredeterminada);
  }

  @Override
  @Transactional
  public void enviarEmailDeRecuperacion(String email, String host) {
    Usuario usuario = usuarioRepository.findByEmailAndEliminadoAndHabilitado(email, false, true);
    if (usuario != null) {
      String passwordRecoveryKey = RandomStringUtils.random(250, true, true);
      this.actualizarPasswordRecoveryKey(passwordRecoveryKey, usuario.getId_Usuario());
      correoElectronicoService.enviarMail(
          usuario.getEmail(),
          "Recuperación de contraseña",
          MessageFormat.format(
              ResourceBundle.getBundle("Mensajes").getString("mensaje_correo_recuperacion"),
              host,
              passwordRecoveryKey,
              usuario.getId_Usuario()));
    } else
      throw new ServiceException(
          ResourceBundle.getBundle("Mensajes").getString("mensaje_correo_no_existente"));
  }

  @Override
  public Usuario guardar(Usuario usuario) {
    this.validarOperacion(TipoDeOperacion.ALTA, usuario);
    usuario.setPassword(this.encriptarConMD5(usuario.getPassword()));
    usuario = usuarioRepository.save(usuario);
    logger.warn("El Usuario {} se guardó correctamente.", usuario);
    return usuario;
  }

  @Override
  public void eliminar(long idUsuario) {
    Usuario usuario = this.getUsuarioPorId(idUsuario);
    this.validarOperacion(TipoDeOperacion.ELIMINACION, usuario);
    clienteService.desvincularClienteDeCredencial(idUsuario);
    usuario.setEliminado(true);
    usuarioRepository.save(usuario);
    logger.warn("El Usuario " + usuario + " se eliminó correctamente.");
  }
  
}
