package sic.modelo.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import sic.modelo.Producto;

@Data
@EqualsAndHashCode(of = "producto")
@AllArgsConstructor
@NoArgsConstructor
public class RenglonPedidoDTO implements Serializable {

    private long id_RenglonPedido;
    private ProductoDTO producto;
    private long idProducto;
    private String codigo;
    private String descripcion;
    private BigDecimal precioDeLista;
    private BigDecimal cantidad;
    private BigDecimal descuento_porcentaje;
    private BigDecimal descuento_neto;
    private BigDecimal subTotal;

}
