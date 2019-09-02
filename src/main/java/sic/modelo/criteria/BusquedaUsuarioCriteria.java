package sic.modelo.criteria;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Pageable;
import sic.modelo.Rol;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BusquedaUsuarioCriteria {

    private boolean buscarPorNombreDeUsuario;
    private String username;
    private boolean buscaPorNombre;
    private String nombre;
    private boolean buscaPorApellido;
    private String apellido;
    private boolean buscaPorEmail;
    private String email;
    private boolean buscarPorRol;
    private List<Rol> roles;
    private Pageable pageable;

}
