package sic.service;

import sic.modelo.Rol;

import java.util.List;

public interface IAuthService {

    void autorizarAcceso(List<Rol> roles, long idUsuarioLoggedIn);

}
