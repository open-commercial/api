package sic.modelo.dto;

import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(of = {"nombre"})
public class UsuarioDTO implements Serializable {

    private long id_Usuario;
    private String nombre;
    private String password;
    private boolean eliminado;

    @Override
    public String toString() {
        return nombre;
    }
}
