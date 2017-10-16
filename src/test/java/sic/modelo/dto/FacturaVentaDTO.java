package sic.modelo.dto;

import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sic.builder.ClienteBuilder;
import sic.builder.UsuarioBuilder;
import sic.modelo.Cliente;
import sic.modelo.Usuario;

@Data
@EqualsAndHashCode(callSuper = true)
public class FacturaVentaDTO extends FacturaDTO implements Serializable {

    private Cliente cliente = new ClienteBuilder().build();
    private Usuario usuario = new UsuarioBuilder().build();

}
