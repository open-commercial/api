package sic.modelo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BusquedaUsuarioCriteria {

    private String username;
    private String nombre;
    private String apellido;
    private String email;
    private List<Rol> roles;
    private Integer pagina;
    private String ordenarPor;
    private String sentido;
}
