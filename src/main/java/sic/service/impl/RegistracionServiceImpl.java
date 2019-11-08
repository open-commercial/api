package sic.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sic.modelo.Cliente;
import sic.modelo.Usuario;
import sic.service.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Locale;

@Service
@Transactional
public class RegistracionServiceImpl implements IRegistracionService {

  private final IUsuarioService usuarioService;
  private final IClienteService clienteService;
  private final ICorreoElectronicoService correoElectronicoService;
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final MessageSource messageSource;

  @Autowired
  public RegistracionServiceImpl(
      IUsuarioService usuarioService,
      IClienteService clienteService,
      ICorreoElectronicoService correoElectronicoService,
      MessageSource messageSource) {
    this.usuarioService = usuarioService;
    this.clienteService = clienteService;
    this.correoElectronicoService = correoElectronicoService;
    this.messageSource = messageSource;
  }

  @Override
  public void crearCuentaConClienteAndUsuario(Cliente cliente, Usuario usuario) {
    usuario.setUsername(this.generarUsername(usuario.getNombre(), usuario.getApellido()));
    Usuario credencial = usuarioService.guardar(usuario);
    cliente.setCredencial(credencial);
    cliente.setBonificacion(BigDecimal.ZERO);
    cliente.setFechaAlta(LocalDateTime.now());
    clienteService.guardar(cliente);
    correoElectronicoService.enviarEmail(
        usuario.getEmail(),
        "",
        "Registración de cuenta nueva",
        messageSource.getMessage(
            "mensaje_correo_registracion",
            new Object[] {
              usuario.getNombre() + " " + usuario.getApellido(),
              cliente.getCategoriaIVA(),
              cliente.getNombreFiscal(),
              cliente.getTelefono(),
              usuario.getUsername(),
            },
            Locale.getDefault()),
        null,
        null);
    logger.warn("El mail de registración para el usuario {} se envió.", usuario.getUsername());
  }

  @Override
  public String generarUsername(String nombre, String apellido) {
    long min = 1L;
    long max = 999L; // 3 digitos
    long randomLong;
    boolean esRepetido = true;
    nombre = nombre.replaceAll("\\s+", "");
    apellido = apellido.replaceAll("\\s+", "");
    String nuevoUsername = "";
    while (esRepetido) {
      randomLong = min + (long) (Math.random() * (max - min));
      nuevoUsername = nombre + apellido + Long.toString(randomLong);
      Usuario u = usuarioService.getUsuarioPorUsername(nuevoUsername);
      if (u == null) esRepetido = false;
    }
    return nuevoUsername.toLowerCase();
  }
}
