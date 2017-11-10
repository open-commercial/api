package sic.modelo.dto;

import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(of = "producto")
public class RenglonPedidoDTO implements Serializable {

    private long id_RenglonPedido;
    private ProductoDTO producto;
    private double cantidad;
    private double descuento_porcentaje;
    private double descuento_neto;
    private double subTotal;

}
