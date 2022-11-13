package sic.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import sic.modelo.UsuarioDTODto;

@RestController
public class UsuarioApiDelegate implements UsuariosApi {

    @Override
    public ResponseEntity<UsuarioDTODto> getUsuarioPorId(String idUsuario) {

        //Service Call
        return UsuariosApi.super.getUsuarioPorId(idUsuario);
    }

    @Override
    public ResponseEntity<Void> eliminar(String idUsuario) {

        // Service Call
        return UsuariosApi.super.eliminar(idUsuario);
    }

    @Override
    public ResponseEntity<Void> guardar(String authorizationHeader, UsuarioDTODto body) {

        // Service Call
        return UsuariosApi.super.guardar(authorizationHeader, body);
    }
}
