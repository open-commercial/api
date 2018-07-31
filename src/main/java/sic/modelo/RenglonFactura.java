package sic.modelo;

import java.io.Serializable;
import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "renglonfactura")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"id_ProductoItem", "codigoItem"})
@ToString
public class RenglonFactura implements Serializable {

    @Id
    @GeneratedValue
    private long id_RenglonFactura;

    private long id_ProductoItem;

    @Column(nullable = false)
    private String codigoItem;

    @Column(nullable = false)
    private String descripcionItem;

    @Column(nullable = false)
    private String medidaItem;

    @Column(precision = 25, scale = 15)
    @DecimalMin(value = "0", message = "{mensaje_renglon_cantidad_mayor_uno}", inclusive = false)
    private BigDecimal cantidad;
    
    @Column(precision = 25, scale = 15)
    @DecimalMin(value = "0", message = "{mensaje_renglon_precio_unitario_negativo}")
    private BigDecimal precioUnitario;
    
    @Column(precision = 25, scale = 15)
    @DecimalMin(value = "0", message = "{mensaje_renglon_descuento_porcentaje_negativo}")
    private BigDecimal descuento_porcentaje;
    
    @Column(precision = 25, scale = 15)
    @DecimalMin(value = "0", message = "{mensaje_renglon_descuento_neto_negativo}")
    private BigDecimal descuento_neto;
    
    @Column(precision = 25, scale = 15)
    @DecimalMin(value = "0", message = "{mensaje_renglon_iva_porcentaje_negativo}")
    private BigDecimal iva_porcentaje;
    
    @Column(precision = 25, scale = 15)
    @DecimalMin(value = "0", message = "{mensaje_renglon_iva_neto_negativo}")
    private BigDecimal iva_neto;
    
    @Column(precision = 25, scale = 15)
    @DecimalMin(value = "0", message = "{mensaje_renglon_impuesto_porcentaje_negativo}")
    private BigDecimal impuesto_porcentaje;
    
    @Column(precision = 25, scale = 15)
    @DecimalMin(value = "0", message = "{mensaje_renglon_impuesto_neto_negativo}")
    private BigDecimal impuesto_neto;
    
    @Column(precision = 25, scale = 15)
    @DecimalMin(value = "0", message = "{mensaje_renglon_ganancia_porcentaje_negativa}")
    private BigDecimal ganancia_porcentaje;
    
    @Column(precision = 25, scale = 15)
    @DecimalMin(value = "0", message = "{mensaje_renglon_ganancia_neta_negativa}")
    private BigDecimal ganancia_neto;
    
    @Column(precision = 25, scale = 15)
    @DecimalMin(value = "0", message = "{mensaje_renglon_importe_negativo}")
    private BigDecimal importe;

}
