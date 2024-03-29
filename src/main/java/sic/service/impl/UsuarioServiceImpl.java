package sic.service.impl;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;
import javax.persistence.EntityNotFoundException;
import com.querydsl.core.BooleanBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sic.exception.UnauthorizedException;
import sic.modelo.*;
import sic.modelo.criteria.BusquedaUsuarioCriteria;
import sic.service.*;
import sic.repository.UsuarioRepository;
import sic.exception.BusinessServiceException;
import sic.util.CustomValidator;
import sic.util.EncryptUtils;

@Service
@Slf4j
public class UsuarioServiceImpl implements IUsuarioService {

  @Value("${EMAIL_DEFAULT_PROVIDER}")
  private String emailDefaultProvider;

  private final UsuarioRepository usuarioRepository;
  private final ISucursalService sucursalService;
  private final IClienteService clienteService;
  private final EmailServiceFactory emailServiceFactory;
  private static final int TAMANIO_PAGINA_DEFAULT = 50;
  private final MessageSource messageSource;
  private final CustomValidator customValidator;
  private final EncryptUtils encryptUtils;

  @Autowired
  @Lazy
  public UsuarioServiceImpl(
    UsuarioRepository usuarioRepository,
    ISucursalService sucursalService,
    IClienteService clienteService,
    EmailServiceFactory emailServiceFactory,
    MessageSource messageSource,
    CustomValidator customValidator,
    EncryptUtils encryptUtils) {
    this.usuarioRepository = usuarioRepository;
    this.sucursalService = sucursalService;
    this.clienteService = clienteService;
    this.emailServiceFactory = emailServiceFactory;
    this.messageSource = messageSource;
    this.customValidator = customValidator;
    this.encryptUtils = encryptUtils;
  }

  @Override
  public Usuario getUsuarioNoEliminadoPorId(Long idUsuario) {
    Optional<Usuario> usuario = usuarioRepository.findByIdUsuario(idUsuario);
    if (usuario.isPresent() && !usuario.get().isEliminado()) {
      return usuario.get();
    } else {
      throw new EntityNotFoundException(messageSource.getMessage(
        "mensaje_usuario_no_existente", null, Locale.getDefault()));
    }
  }

  @Override
  public Usuario getUsuarioPorUsername(String username) {
    return this.usuarioRepository.findByUsernameAndEliminado(username, false);
  }

  @Override
  public Usuario getUsuarioPorPasswordRecoveryKeyAndIdUsuario(String passwordRecoveryKey, long idUsuario) {
    var usuario = usuarioRepository.findByPasswordRecoveryKeyAndIdUsuarioAndEliminadoAndHabilitado(
            passwordRecoveryKey, idUsuario);
    if (usuario == null || LocalDateTime.now().isAfter(usuario.getPasswordRecoveryKeyExpirationDate())) {
      throw new UnauthorizedException(
          messageSource.getMessage("mensaje_error_passwordRecoveryKey", null, Locale.getDefault()));
    }
    return usuario;
  }

  @Override
  public Usuario autenticarUsuario(Credencial credencial) {
    Usuario usuario =
        usuarioRepository.findByUsernameOrEmailAndPasswordAndEliminado(
            credencial.getUsername(),
            credencial.getUsername(),
            encryptUtils.encryptWithMD5(credencial.getPassword()));
    if (usuario == null) {
      throw new BusinessServiceException(messageSource.getMessage(
        "mensaje_usuario_logInInvalido", null, Locale.getDefault()));
    }
    if (!usuario.isHabilitado()) {
      throw new BusinessServiceException(messageSource.getMessage(
        "mensaje_usuario_no_habilitado", null, Locale.getDefault()));
    }
    return usuario;
  }

  @Override
  public Page<Usuario> buscarUsuarios(BusquedaUsuarioCriteria criteria) {
    QUsuario qUsuario = QUsuario.usuario;
    BooleanBuilder builder = new BooleanBuilder();
    if (criteria.getApellido() != null) {
      String[] terminos = criteria.getApellido().split(" ");
      BooleanBuilder rsPredicate = new BooleanBuilder();
      for (String termino : terminos) {
        rsPredicate.and(qUsuario.apellido.containsIgnoreCase(termino));
      }
      builder.or(rsPredicate);
    }
    if (criteria.getNombre() != null) {
      String[] terminos = criteria.getNombre().split(" ");
      BooleanBuilder rsPredicate = new BooleanBuilder();
      for (String termino : terminos) {
        rsPredicate.and(qUsuario.nombre.containsIgnoreCase(termino));
      }
      builder.or(rsPredicate);
    }
    if (criteria.getUsername() != null) {
      String[] terminos = criteria.getUsername().split(" ");
      BooleanBuilder rsPredicate = new BooleanBuilder();
      for (String termino : terminos) {
        rsPredicate.and(qUsuario.username.containsIgnoreCase(termino));
      }
      builder.or(rsPredicate);
    }
    if (criteria.getEmail() != null) {
      String[] terminos = criteria.getEmail().split(" ");
      BooleanBuilder rsPredicate = new BooleanBuilder();
      for (String termino : terminos) {
        rsPredicate.and(qUsuario.email.containsIgnoreCase(termino));
      }
      builder.or(rsPredicate);
    }
    if (criteria.getRoles() != null && !criteria.getRoles().isEmpty()) {
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
    return usuarioRepository.findAll(builder, this.getPageable(criteria.getPagina(), criteria.getOrdenarPor(), criteria.getSentido()));
  }

  private Pageable getPageable(Integer pagina, String ordenarPor, String sentido) {
    String ordenDefault = "username";
    if (pagina == null) pagina = 0;
    if (ordenarPor == null || sentido == null) {
      return PageRequest.of(
          pagina, TAMANIO_PAGINA_DEFAULT, Sort.by(Sort.Direction.ASC, ordenDefault));
    } else {
      switch (sentido) {
        case "ASC":
          return PageRequest.of(
              pagina, TAMANIO_PAGINA_DEFAULT, Sort.by(Sort.Direction.ASC, ordenarPor));
        case "DESC":
          return PageRequest.of(
              pagina, TAMANIO_PAGINA_DEFAULT, Sort.by(Sort.Direction.DESC, ordenarPor));
        default:
          return PageRequest.of(
              pagina, TAMANIO_PAGINA_DEFAULT, Sort.by(Sort.Direction.DESC, ordenDefault));
      }
    }
  }

  @Override
  public void validarReglasDeNegocio(TipoDeOperacion operacion, Usuario usuario) {
    customValidator.validar(usuario);
    // Username sin espacios en blanco
    if (usuario.getUsername().contains(" ")) {
      throw new BusinessServiceException(messageSource.getMessage(
        "mensaje_usuario_username_con_espacios", null, Locale.getDefault()));
    }
    // Duplicados
    if (operacion == TipoDeOperacion.ALTA) {
      // username
      if (usuarioRepository.findByUsernameAndEliminado(usuario.getUsername(), false) != null) {
        throw new BusinessServiceException(messageSource.getMessage(
          "mensaje_usuario_duplicado_username", null, Locale.getDefault()));
      }
      // email
      if (usuarioRepository.findByEmailAndEliminado(usuario.getEmail(), false) != null) {
        throw new BusinessServiceException(messageSource.getMessage(
          "mensaje_usuario_duplicado_email", null, Locale.getDefault()));
      }
    }
    if (operacion == TipoDeOperacion.ACTUALIZACION) {
      // username
      var usuarioGuardado = usuarioRepository.findByUsernameAndEliminado(usuario.getUsername(), false);
      if (usuarioGuardado != null && usuarioGuardado.getIdUsuario() != usuario.getIdUsuario()) {
        throw new BusinessServiceException(messageSource.getMessage(
          "mensaje_usuario_duplicado_username", null, Locale.getDefault()));
      }
      // email
      usuarioGuardado = usuarioRepository.findByEmailAndEliminado(usuario.getEmail(), false);
      if (usuarioGuardado != null && usuarioGuardado.getIdUsuario() != usuario.getIdUsuario()) {
        throw new BusinessServiceException(messageSource.getMessage(
          "mensaje_usuario_duplicado_email", null, Locale.getDefault()));
      }
    }
    // Ultimo usuario administrador
    if ((operacion == TipoDeOperacion.ACTUALIZACION && !usuario.getRoles().contains(Rol.ADMINISTRADOR))
        || operacion == TipoDeOperacion.ELIMINACION && usuario.getRoles().contains(Rol.ADMINISTRADOR)) {
      var administradores = this.getUsuariosPorRol(Rol.ADMINISTRADOR).getContent();
      if (administradores.size() == 1 && administradores.get(0).getIdUsuario() == usuario.getIdUsuario()) {
        throw new BusinessServiceException(messageSource.getMessage(
          "mensaje_usuario_ultimoAdmin", null, Locale.getDefault()));
      }
    }
  }

  @Override
  public Page<Usuario> getUsuariosPorRol(Rol rol) {
    Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);
    return usuarioRepository.findAllByRolesContainsAndEliminado(rol, false, pageable);
  }

  @Override
  @Transactional
  public void actualizar(Usuario usuario) {
    this.validarReglasDeNegocio(TipoDeOperacion.ACTUALIZACION, usuario);
    if (!usuario.getRoles().contains(Rol.VIAJANTE)) {
      this.clienteService.desvincularClienteDeViajante(usuario.getIdUsuario());
    }
    if (!usuario.getRoles().contains(Rol.COMPRADOR)) {
      this.clienteService.desvincularClienteDeCredencial(usuario.getIdUsuario());
    }
    usuario.setUsername(usuario.getUsername().toLowerCase());
    usuarioRepository.save(usuario);
    log.info("El Usuario {} se actualizó correctamente.", usuario);
  }

  @Override
  public void actualizarPasswordRecoveryKey(String key, Usuario usuario) {
    usuario.setPasswordRecoveryKey(key);
    usuario.setPasswordRecoveryKeyExpirationDate(LocalDateTime.now().plusHours(3L));
    usuarioRepository.save(usuario);
  }

  @Override
  public void actualizarPasswordConRecuperacion(String key, long idUsuario, String newPassword) {
    var usuario = this.getUsuarioPorPasswordRecoveryKeyAndIdUsuario(key, idUsuario);
    usuario.setPassword(encryptUtils.encryptWithMD5(newPassword));
    usuario.setPasswordRecoveryKey(null);
    usuario.setPasswordRecoveryKeyExpirationDate(null);
    this.validarReglasDeNegocio(TipoDeOperacion.ACTUALIZACION, usuario);
    usuarioRepository.save(usuario);
  }

  @Override
  public void actualizarIdSucursalDeUsuario(long idUsuario, long idSucursalPredeterminada) {
    if (sucursalService.getSucursalPorId(idSucursalPredeterminada) == null) {
      throw new EntityNotFoundException(messageSource.getMessage(
        "mensaje_sucursal_no_existente", null, Locale.getDefault()));
    }
    var usuario = this.getUsuarioNoEliminadoPorId(idUsuario);
    usuario.setIdSucursalPredeterminada(idSucursalPredeterminada);
    usuarioRepository.save(usuario);
  }

  @Override
  @Transactional
  public void enviarEmailDeRecuperacion(long idSucursal, String email, String host) {
    Usuario usuario = usuarioRepository.findByEmailAndEliminado(email, false);
    if (usuario == null || !usuario.isHabilitado()) {
      throw new BusinessServiceException(
          messageSource.getMessage(
              "mensaje_correo_no_existente_or_deshabilitado", null, Locale.getDefault()));
    }
    String passwordRecoveryKey = RandomStringUtils.random(250, true, true);
    this.actualizarPasswordRecoveryKey(passwordRecoveryKey, usuario);
    emailServiceFactory.getEmailService(emailDefaultProvider)
            .enviarEmail(
                    usuario.getEmail(),
                    "",
                    "Recuperación de contraseña",
                    messageSource.getMessage(
                            "mensaje_correo_recuperacion",
                            new Object[]{host, passwordRecoveryKey, usuario.getIdUsuario()},
                            Locale.getDefault()),
                    null,
                    null);
  }

  @Override
  public Usuario guardar(Usuario usuario) {
    this.validarReglasDeNegocio(TipoDeOperacion.ALTA, usuario);
    usuario.setUsername(usuario.getUsername().toLowerCase());
    usuario.setPassword(encryptUtils.encryptWithMD5(usuario.getPassword()));
    usuario = usuarioRepository.save(usuario);
    log.info("El Usuario {} se guardó correctamente.", usuario);
    return usuario;
  }

  @Override
  public void eliminar(long idUsuario) {
    Usuario usuario = this.getUsuarioNoEliminadoPorId(idUsuario);
    this.validarReglasDeNegocio(TipoDeOperacion.ELIMINACION, usuario);
    Cliente clienteVinculado = clienteService.getClientePorCredencial(usuario);
    if (clienteVinculado != null)
      throw new BusinessServiceException(
          messageSource.getMessage(
              "mensaje_usuario_eliminar_con_credencial",
              new Object[] {clienteVinculado.getNombreFiscal()},
              Locale.getDefault()));
    usuario.setEliminado(true);
    usuarioRepository.save(usuario);
    log.info("El Usuario {} se eliminó correctamente.", usuario);
  }

  @Override
  public boolean esUsuarioHabilitado(long idUsuario) {
    return this.getUsuarioNoEliminadoPorId(idUsuario).isHabilitado();
  }
}
