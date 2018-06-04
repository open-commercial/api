package sic.modelo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Pageable;

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
    private Empresa empresa;
    private Pageable pageable;

}
