package sic.modelo;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BusquedaCajaCriteria {

    private Date fechaDesde;
    private Date fechaHasta;
    private Long idEmpresa;
    private int cantidadDeRegistros;
    private Long idUsuarioApertura;
    private Long idUsuarioCierre;
    private Integer pagina;
    private String ordenarPor;
    private String sentido;
    
}
