package sic.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import sic.modelo.CategoriaIVA;
import sic.modelo.Cliente;
import sic.modelo.Rol;
import sic.modelo.Usuario;
import sic.modelo.dto.RegistracionClienteAndUsuarioDTO;
import sic.service.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Locale;

@Service
@Transactional
@Validated
public class RegistracionServiceImpl implements IRegistracionService {

  @Value("${SIC_MAIL_USERNAME}")
  private String emailUsername;

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
  @Validated
  public void crearCuenta(@Valid RegistracionClienteAndUsuarioDTO registracionClienteAndUsuarioDTO) {
    Usuario nuevoUsuario = new Usuario();
    nuevoUsuario.setHabilitado(true);
    nuevoUsuario.setNombre(registracionClienteAndUsuarioDTO.getNombre());
    nuevoUsuario.setApellido(registracionClienteAndUsuarioDTO.getApellido());
    nuevoUsuario.setEmail(registracionClienteAndUsuarioDTO.getEmail());
    nuevoUsuario.setPassword(registracionClienteAndUsuarioDTO.getPassword());
    nuevoUsuario.setRoles(Collections.singletonList(Rol.COMPRADOR));
    Cliente nuevoCliente = new Cliente();
    nuevoCliente.setTelefono(registracionClienteAndUsuarioDTO.getTelefono());
    nuevoCliente.setEmail(registracionClienteAndUsuarioDTO.getEmail());
    CategoriaIVA categoriaIVA = registracionClienteAndUsuarioDTO.getCategoriaIVA();
    if (categoriaIVA == CategoriaIVA.CONSUMIDOR_FINAL) {
      nuevoCliente.setNombreFiscal(
              registracionClienteAndUsuarioDTO.getNombre()
                      + " "
                      + registracionClienteAndUsuarioDTO.getApellido());
      nuevoCliente.setCategoriaIVA(CategoriaIVA.CONSUMIDOR_FINAL);
    } else if (categoriaIVA == CategoriaIVA.RESPONSABLE_INSCRIPTO
            || categoriaIVA == CategoriaIVA.MONOTRIBUTO
            || categoriaIVA == CategoriaIVA.EXENTO) {
      nuevoCliente.setNombreFiscal(registracionClienteAndUsuarioDTO.getNombreFiscal());
      nuevoCliente.setCategoriaIVA(categoriaIVA);
    }
    nuevoUsuario.setUsername(this.generarUsername(nuevoUsuario.getNombre(), nuevoUsuario.getApellido()));
    Usuario credencial = usuarioService.guardar(nuevoUsuario);
    nuevoCliente.setCredencial(credencial);
    nuevoCliente.setFechaAlta(LocalDateTime.now());
    nuevoCliente.setMontoCompraMinima(BigDecimal.ZERO);
    clienteService.guardar(nuevoCliente);
    correoElectronicoService.enviarEmail(
        nuevoUsuario.getEmail(),
        this.emailUsername,
        "Registración de cuenta nueva",
        messageSource.getMessage(
            "mensaje_correo_registracion",
            new Object[] {
              nuevoUsuario.getNombre() + " " + nuevoUsuario.getApellido(),
              nuevoCliente.getCategoriaIVA(),
              nuevoCliente.getNombreFiscal(),
              nuevoCliente.getTelefono(),
              nuevoUsuario.getUsername(),
            },
            Locale.getDefault()),
        null,
        null);
    logger.warn("El mail de registración para el usuario {} se envió.", nuevoUsuario.getUsername());
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
