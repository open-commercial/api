package sic.modelo.dto;

import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class NotaDebitoClienteDTO extends NotaDebitoDTO implements Serializable {
    
    private ClienteDTO cliente;
    
}
