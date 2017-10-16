package sic.modelo;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Pageable;
import sic.modelo.Empresa;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BusquedaNotaCriteria {
    
    private boolean buscaPorFecha;
    private Date fechaDesde;
    private Date fechaHasta;
    private Empresa empresa;
    private int cantidadDeRegistros;
    private Cliente cliente;
    private Pageable pageable;
    
}
