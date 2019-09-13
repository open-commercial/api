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
    
    private boolean buscaPorFecha;
    private Date fechaDesde;
    private Date fechaHasta;
    private Long idEmpresa;
    private int cantidadDeRegistros;
    private boolean buscaPorUsuarioApertura;
    private Long idUsuarioApertura;
    private boolean buscaPorUsuarioCierre;
    private Long idUsuarioCierre;
    private Integer pagina;
    private String ordenarPor;
    private String sentido;
    
}
