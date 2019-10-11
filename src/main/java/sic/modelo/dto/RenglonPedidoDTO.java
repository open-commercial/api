package sic.modelo.dto;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RenglonPedidoDTO implements Serializable {

    private long id_RenglonPedido;
    private long idProductoItem;
    private String codigoItem;
    private String descripcionItem;
    private String medidaItem;
    private String urlImagenItem;
    private boolean oferta;
    private BigDecimal precioUnitario;
    private BigDecimal cantidad;
    private BigDecimal bonificacionPorcentaje;
    private BigDecimal bonificacionNeta;
    private BigDecimal subTotal;
    private BigDecimal importeAnterior;
    private BigDecimal importe;

}
