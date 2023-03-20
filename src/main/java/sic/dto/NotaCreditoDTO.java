package sic.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import lombok.*;
import sic.entity.RenglonNotaCredito;

@Data
@EqualsAndHashCode(callSuper = true, exclude = {"renglonesNotaCredito"})
@AllArgsConstructor
@NoArgsConstructor
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
