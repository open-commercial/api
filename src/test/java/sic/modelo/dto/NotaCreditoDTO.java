package sic.modelo.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sic.modelo.RenglonNotaCredito;

@Data
@EqualsAndHashCode(callSuper = true)
public class NotaCreditoDTO extends NotaDTO implements Serializable {
    
    private List<RenglonNotaCredito> renglonesNotaCredito;
    private BigDecimal subTotal = new BigDecimal(6500);
    private BigDecimal recargoPorcentaje = BigDecimal.ZERO;
    private BigDecimal recargoNeto = BigDecimal.ZERO;
    private BigDecimal descuentoPorcentaje = BigDecimal.ZERO;
    private BigDecimal descuentoNeto = BigDecimal.ZERO;
    
}
