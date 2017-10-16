package sic.modelo.dto;

import java.io.Serializable;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sic.modelo.RenglonNotaCredito;

@Data
@EqualsAndHashCode(callSuper = true)
public class NotaCreditoDTO extends NotaDTO implements Serializable {
    
    private List<RenglonNotaCredito> renglonesNotaCredito;
    private double subTotal = 6500;
    private double recargoPorcentaje = 0.0;
    private double recargoNeto = 0.0;
    private double descuentoPorcentaje = 0.0;
    private double descuentoNeto = 0.0;
    
}
