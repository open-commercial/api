package sic.modelo.criteria;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BusquedaTransportistaCriteria {

    private boolean buscarPorNombre;
    private String nombre;
    private boolean buscarPorProvincia;
    private Long idProvincia;
    private boolean buscarPorLocalidad;
    private Long idLocalidad;
    private Integer pagina;
    private String ordenarPor;
    private String sentido;
}
