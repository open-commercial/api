package sic.modelo;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Pageable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BusquedaCajaCriteria {
    
    private boolean buscaPorFecha;
    private Date fechaDesde;
    private Date fechaHasta;
    private Long idSucursal;
    private int cantidadDeRegistros;
    private boolean buscaPorUsuarioApertura;
    private Long idUsuarioApertura;
    private boolean buscaPorUsuarioCierre;
    private Long idUsuarioCierre;
    private Pageable pageable;
    
}
