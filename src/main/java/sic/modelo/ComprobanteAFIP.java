package sic.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

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
    private Empresa empresa;    
    private Cliente cliente;    
    private BigDecimal subtotalBruto;    
    private BigDecimal iva105neto;    
    private BigDecimal iva21neto;    
    private BigDecimal montoNoGravado;
    private BigDecimal total;
}
