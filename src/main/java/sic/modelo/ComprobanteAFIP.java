package sic.modelo;

import java.math.BigDecimal;
import java.util.Date;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ComprobanteAFIP {
    
    private Date fecha;
    private TipoDeComprobante tipoComprobante;
    private long CAE;
    private Date vencimientoCAE;
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
