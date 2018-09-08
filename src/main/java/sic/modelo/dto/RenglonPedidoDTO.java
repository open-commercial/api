package sic.modelo.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RenglonPedidoDTO implements Serializable {

    private long id_RenglonPedido;
    private long id_Producto;
    private String codigoProducto;
    private String descripcionProducto;
    private BigDecimal precioDeListaProducto;
    private BigDecimal cantidad;
    private BigDecimal descuento_porcentaje;
    private BigDecimal descuento_neto;
    private BigDecimal subTotal;

}
