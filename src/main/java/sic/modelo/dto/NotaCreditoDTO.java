package sic.modelo.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sic.modelo.RenglonNotaCredito;

@Data
@EqualsAndHashCode(callSuper = true, exclude = {"renglonesNotaCredito"})
@Builder
public class NotaCreditoDTO extends NotaDTO implements Serializable {
    
    private boolean modificaStock;
    private List<RenglonNotaCredito> renglonesNotaCredito;
    private BigDecimal subTotal;
    private BigDecimal recargoPorcentaje;
    private BigDecimal recargoNeto;
    private BigDecimal descuentoPorcentaje;
    private BigDecimal descuentoNeto;
    
}
