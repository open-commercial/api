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
public class BusquedaFacturaCompraCriteria {

    private boolean buscaPorFecha;
    private Date fechaDesde;
    private Date fechaHasta;
    private boolean buscaPorProveedor;
    private Long idProveedor;
    private boolean buscaPorNumeroFactura;
    private long numSerie;
    private long numFactura;
    private boolean buscaPorTipoComprobante;
    private TipoDeComprobante tipoComprobante;
    private Long idEmpresa;
    private int cantRegistros;
    private Pageable pageable;

}
