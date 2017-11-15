package sic.modelo.dto;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(of = "producto")
@AllArgsConstructor
@NoArgsConstructor
public class RenglonPedidoDTO implements Serializable {

    private long id_RenglonPedido;
    private ProductoDTO producto;
    private double cantidad;
    private double descuento_porcentaje;
    private double descuento_neto;
    private double subTotal;

}
