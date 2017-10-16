package sic.service.impl;

import java.util.List;
import java.util.ResourceBundle;
import javax.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sic.modelo.Rol;
import sic.modelo.Usuario;
import sic.service.IUsuarioService;
import sic.service.BusinessServiceException;
import sic.modelo.TipoDeOperacion;
import sic.util.Utilidades;
import sic.util.Validator;
import sic.repository.UsuarioRepository;

@Service
public class UsuarioServiceImpl implements IUsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public UsuarioServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public Usuario getUsuarioPorId(Long idUsuario) {
        Usuario usuario = usuarioRepository.findOne(idUsuario);
        if (usuario == null) {
            throw new EntityNotFoundException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_usuario_no_existente"));
        }
        return usuario;
    }
    
    @Override
    public Usuario getUsuarioPorNombre(String nombre) {
        Usuario usuario = usuarioRepository.findByNombreAndEliminado(nombre, false);
//        if (usuario == null) {
//            throw new EntityNotFoundException(ResourceBundle.getBundle("Mensajes")
//                    .getString("mensaje_usuario_no_existente"));
//        }
        return usuario;
    }
    
    @Override
    public List<Usuario> getUsuarios() {
        return usuarioRepository.findAllByAndEliminadoOrderByNombreAsc(false);
    }

    @Override
    public List<Usuario> getUsuariosPorRol(Rol rol) {
        return usuarioRepository.findAllByAndEliminadoAndRolesOrderByNombreAsc(false, rol);
    }

    @Override
    public List<Usuario> getUsuariosAdministradores() {
        return usuarioRepository.findAllByAndRolesAndEliminadoOrderByNombreAsc(Rol.ADMINISTRADOR, false);
    }

    @Override
    public Usuario getUsuarioPorNombreContrasenia(String nombre, String contrasenia) {
        Usuario usuario = usuarioRepository.findByNombreAndPasswordAndEliminado(nombre, contrasenia, false);
        if (usuario == null) {
            throw new EntityNotFoundException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_usuario_no_existente"));
        }
        return usuario;
    }

    private void validarOperacion(TipoDeOperacion operacion, Usuario usuario) {
        //Requeridos
        if (Validator.esVacio(usuario.getNombre())) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_usuario_vacio_nombre"));
        }
        if (Validator.esVacio(usuario.getPassword())) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_usuario_vacio_password"));
        }
        if (usuario.getRoles().isEmpty()) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_usuario_no_selecciono_rol"));
        }
        //Duplicados
        //Nombre
        Usuario usuarioDuplicado = this.getUsuarioPorNombre(usuario.getNombre());
        if (operacion.equals(TipoDeOperacion.ALTA) && usuarioDuplicado != null) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_usuario_duplicado_nombre"));
        }
        if (operacion.equals(TipoDeOperacion.ACTUALIZACION)) {
            if (usuarioDuplicado != null && usuarioDuplicado.getId_Usuario() != usuario.getId_Usuario()) {
                throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_usuario_duplicado_nombre"));
            }
        }
        //Ultimo usuario administrador
        if ((operacion == TipoDeOperacion.ACTUALIZACION && usuario.getRoles().contains(Rol.ADMINISTRADOR) == false)
                || operacion == TipoDeOperacion.ELIMINACION && usuario.getRoles().contains(Rol.ADMINISTRADOR) == true) {
            List<Usuario> adminitradores = this.getUsuariosAdministradores();
            if (adminitradores.size() == 1) {
                if (adminitradores.get(0).getId_Usuario() == usuario.getId_Usuario()) {
                    throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                            .getString("mensaje_usuario_ultimoAdmin"));
                }
            }
        }
    }

    @Override
    @Transactional
    public void actualizar(Usuario usuario) {
        this.validarOperacion(TipoDeOperacion.ACTUALIZACION, usuario);
        Usuario usuarioDB = usuarioRepository.findOne(usuario.getId_Usuario());
        if (!usuario.getPassword().equals(usuarioDB.getPassword())) {
            usuario.setPassword(Utilidades.encriptarConMD5(usuario.getPassword()));
        }
        usuarioRepository.save(usuario);
    }

    @Override
    @Transactional
    public Usuario guardar(Usuario usuario) {
        this.validarOperacion(TipoDeOperacion.ALTA, usuario);
        usuario.setPassword(Utilidades.encriptarConMD5(usuario.getPassword()));
        usuario = usuarioRepository.save(usuario);
        LOGGER.warn("El Usuario " + usuario + " se guardó correctamente.");
        return usuario;
    }

    @Override
    @Transactional
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
   
}
