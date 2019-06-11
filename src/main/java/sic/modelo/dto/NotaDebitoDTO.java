package sic.modelo.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sic.modelo.RenglonNotaDebito;

@Data
@EqualsAndHashCode(callSuper = true, exclude = {"renglonesNotaDebito"})
public class NotaDebitoDTO extends NotaDTO implements Serializable {
    
    private List<RenglonNotaDebito> renglonesNotaDebito;
    private BigDecimal montoNoGravado;
    private Long idRecibo;
    private boolean pagada;
    
}
