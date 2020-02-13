package sic.modelo;

import java.io.Serializable;
import java.math.BigDecimal;
import javax.persistence.*;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import sic.controller.Views;

@Entity
@Table(name = "renglonfactura")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"idProductoItem", "codigoItem"})
@ToString
@JsonView(Views.Comprador.class)
public class RenglonFactura implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_RenglonFactura")
    private long idRenglonFactura;

    private long idProductoItem;

    @Column(nullable = false)
    private String codigoItem;

    @Column(nullable = false)
    private String descripcionItem;

    @Column(nullable = false)
    private String medidaItem;

    @Column(precision = 25, scale = 15)
    @NotNull
    @Positive(message = "{mensaje_renglon_cantidad_mayor_uno}")
    private BigDecimal cantidad;
    
    @Column(precision = 25, scale = 15)
    @DecimalMin(value = "0", message = "{mensaje_renglon_precio_unitario_negativo}")
    private BigDecimal precioUnitario;
    
    @Column(precision = 25, scale = 15)
    @DecimalMin(value = "0", message = "{mensaje_renglon_bonificacion_porcentaje_negativa}")
    private BigDecimal bonificacionPorcentaje;
    
    @Column(precision = 25, scale = 15)
    @DecimalMin(value = "0", message = "{mensaje_renglon_bonificacion_neta_negativa}")
    private BigDecimal bonificacionNeta;
    
    @Column(precision = 25, scale = 15)
    @DecimalMin(value = "0", message = "{mensaje_renglon_iva_porcentaje_negativo}")
    private BigDecimal ivaPorcentaje;
    
    @Column(precision = 25, scale = 15)
    @DecimalMin(value = "0", message = "{mensaje_renglon_iva_neto_negativo}")
    private BigDecimal ivaNeto;
    
    @Column(precision = 25, scale = 15)
    @DecimalMin(value = "0", message = "{mensaje_renglon_ganancia_porcentaje_negativa}")
    private BigDecimal gananciaPorcentaje;
    
    @Column(precision = 25, scale = 15)
    @DecimalMin(value = "0", message = "{mensaje_renglon_ganancia_neta_negativa}")
    private BigDecimal gananciaNeto;
    
    @Column(precision = 25, scale = 15)
    @DecimalMin(value = "0", message = "{mensaje_renglon_importe_negativo}")
    private BigDecimal importe;

}
