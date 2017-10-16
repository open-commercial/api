package sic.modelo.dto;

import java.io.Serializable;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sic.modelo.RenglonNotaDebito;

@Data
@EqualsAndHashCode(callSuper = true)
public class NotaDebitoDTO extends NotaDTO implements Serializable {
    
    private Long pagoId = 0L;
    private List<RenglonNotaDebito> renglonesNotaDebito;
    private double montoNoGravado;
    
}
