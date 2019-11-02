package sic.modelo.criteria;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BusquedaCajaCriteria {

    private LocalDateTime fechaDesde;
    private LocalDateTime fechaHasta;
    private Long idEmpresa;
    private int cantidadDeRegistros;
    private Long idUsuarioApertura;
    private Long idUsuarioCierre;
    private Integer pagina;
    private String ordenarPor;
    private String sentido;
    
}
