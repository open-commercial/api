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
    private long idProductoItem;
    private String codigoItem;
    private String descripcionItem;
    private String medidaItem;
    private BigDecimal precioUnitario;
    private BigDecimal cantidad;
    private BigDecimal descuentoPorcentaje;
    private BigDecimal descuentoNeto;
    private BigDecimal subTotal;

}
