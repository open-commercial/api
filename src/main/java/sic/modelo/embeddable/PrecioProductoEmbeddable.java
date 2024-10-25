package sic.modelo.embeddable;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sic.config.Views;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Embeddable
@JsonView(Views.Vendedor.class)
public class PrecioProductoEmbeddable implements Serializable {

    @Column(precision = 25, scale = 15)
    @DecimalMin(value = "0", message = "{mensaje_producto_precioCosto_negativo}")
    private BigDecimal precioCosto;

    @Column(precision = 25, scale = 15)
    @DecimalMin(value = "0", message = "{mensaje_producto_gananciaPorcentaje_negativo}")
    private BigDecimal gananciaPorcentaje;

    @Column(precision = 25, scale = 15)
    @DecimalMin(value = "0", message = "{mensaje_producto_gananciaNeto_negativo}")
    private BigDecimal gananciaNeto;

    @Column(precision = 25, scale = 15)
    @DecimalMin(value = "0", message = "{mensaje_producto_venta_publico_negativo}")
    private BigDecimal precioVentaPublico;

    @Column(precision = 25, scale = 15)
    @DecimalMin(value = "0", message = "{mensaje_producto_IVAPorcentaje_negativo}")
    private BigDecimal ivaPorcentaje;

    @Column(precision = 25, scale = 15)
    @DecimalMin(value = "0", message = "{mensaje_producto_IVANeto_negativo}")
    private BigDecimal ivaNeto;

    @Column(precision = 25, scale = 15)
    @DecimalMin(value = "0", message = "{mensaje_producto_precioLista_negativo}")
    @JsonView(Views.Comprador.class)
    private BigDecimal precioLista;

    @JsonView(Views.Comprador.class)
    private boolean oferta;

    @Column(precision = 25, scale = 15)
    @DecimalMax(value = "100", inclusive = false, message = "{mensaje_producto_oferta_superior_100}")
    @JsonView(Views.Comprador.class)
    private BigDecimal porcentajeBonificacionOferta;

    @Column(precision = 25, scale = 15)
    @DecimalMin(value = "0", message = "{mensaje_producto_bonificacion_inferior_0}")
    @DecimalMax(value = "100", inclusive = false, message = "{mensaje_producto_bonificacion_superior_100}")
    @JsonView(Views.Comprador.class)
    private BigDecimal porcentajeBonificacionPrecio;

    @Column(precision = 25, scale = 15)
    @DecimalMin(value = "0",message = "{mensaje_producto_precio_bonificado_igual_menor_cero}")
    @JsonView(Views.Comprador.class)
    private BigDecimal precioBonificado;

}
