package sic.modelo.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sic.modelo.Recibo;
import sic.modelo.RenglonNotaDebito;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonSubTypes({
    @JsonSubTypes.Type(value = NotaDebitoClienteDTO.class, name = "NotaDebitoCliente"), 
    @JsonSubTypes.Type(value = NotaDebitoProveedorDTO.class, name = "NotaDebitoProveedor")
})
public abstract class NotaDebitoDTO extends NotaDTO implements Serializable {
    
    private List<RenglonNotaDebito> renglonesNotaDebito;
    private BigDecimal montoNoGravado;
    private Recibo recibo;    
    private boolean pagada;
    
}
