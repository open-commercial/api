package sic.modelo.dto;

import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sic.modelo.Cliente;

@Data
@EqualsAndHashCode(callSuper = true)
public class NotaDebitoClienteDTO extends NotaDebitoDTO implements Serializable {
    
    private Cliente cliente;
    
}
