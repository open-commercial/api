package org.opencommercial.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class ComprobanteAutorizableAFIP extends ComprobanteAutorizable {

    private TipoDeComprobante tipoComprobante;
    private long cae;
    private LocalDate vencimientoCAE;
    private long numSerieAfip;
    private long numFacturaAfip;
    private BigDecimal iva105neto;    
    private BigDecimal iva21neto;
}
