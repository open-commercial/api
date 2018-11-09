package sic.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sic.modelo.Cliente;
import sic.modelo.Usuario;
import sic.service.*;

import java.text.MessageFormat;
import java.util.ResourceBundle;

@Service
@Transactional
public class RegistracionServiceImpl implements IRegistracionService {

  private final IUsuarioService usuarioService;
  private final IClienteService clienteService;
  private final ICorreoElectronicoService correoElectronicoService;
  private final IConfiguracionDelSistemaService configuracionDelSistemaService;
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("Mensajes");

  @Autowired
  public RegistracionServiceImpl(
      IUsuarioService usuarioService,
      IClienteService clienteService,
      ICorreoElectronicoService correoElectronicoService,
      IConfiguracionDelSistemaService cds) {
    this.usuarioService = usuarioService;
    this.clienteService = clienteService;
    this.correoElectronicoService = correoElectronicoService;
    this.configuracionDelSistemaService = cds;
  }

  @Override
  public void crearCuentaConClienteAndUsuario(Cliente cliente, Usuario usuario) {
    usuario.setUsername(this.generarUsername(usuario.getNombre(), usuario.getApellido()));
    Usuario credencial = usuarioService.guardar(usuario);
    cliente.setCredencial(credencial);
    clienteService.guardar(cliente);
    correoElectronicoService.enviarMailPorEmpresa(
        cliente.getEmpresa().getId_Empresa(),
        usuario.getEmail(),
        configuracionDelSistemaService
            .getConfiguracionDelSistemaPorEmpresa(cliente.getEmpresa())
            .getEmailUsername(),
        "Registración de cuenta nueva",
        MessageFormat.format(
            RESOURCE_BUNDLE.getString("mensaje_correo_registracion"),
            usuario.getNombre() + " " + usuario.getApellido(),
            cliente.getNombreFiscal(),
            cliente.getTelefono(),
            usuario.getUsername()),
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
