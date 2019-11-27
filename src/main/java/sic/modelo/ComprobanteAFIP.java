package sic.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;
import sic.modelo.embeddable.ClienteEmbeddable;

@Data
@Builder
public class ComprobanteAFIP {

    private long idComprobante;
    private LocalDateTime fecha;
    private TipoDeComprobante tipoComprobante;
    private long cae;
    private LocalDate vencimientoCAE;
    private long numSerieAfip;
    private long numFacturaAfip;
    private Sucursal sucursal;
    private ClienteEmbeddable cliente;
    private BigDecimal subtotalBruto;    
    private BigDecimal iva105neto;    
    private BigDecimal iva21neto;    
    private BigDecimal montoNoGravado;
    private BigDecimal total;
}
