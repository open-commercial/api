package sic.modelo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BusquedaTransportistaCriteria {

    private boolean buscarPorNombre;
    private String nombre;
    private boolean buscarPorPais;
    private Long idPais;
    private boolean buscarPorProvincia;
    private Long idProvincia;
    private boolean buscarPorLocalidad;
    private Long idLocalidad;
    private Long idEmpresa;
}
