package sic.modelo;

import java.io.Serializable;
import java.math.BigDecimal;
import javax.persistence.*;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.validator.constraints.NotEmpty;

@Entity
@Table(name = "renglonnotacredito")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"idProductoItem", "codigoItem"})
@ToString
public class RenglonNotaCredito implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long idRenglonNotaCredito;
    
    private Long idProductoItem;

    private String codigoItem;

    @Column(nullable = false)
    @NotEmpty(message = "{mensaje_renglon_credito_vacio_descripcion}")
    @NotNull(message = "{mensaje_renglon_credito_vacio_descripcion}")
    private String descripcionItem;

    private String medidaItem;
        
    @Column(precision = 25, scale = 15)
    @DecimalMin(value = "0", message = "{mensaje_renglon_credito_cantidad_negativa}")
    private BigDecimal cantidad;
    
    @Column(precision = 25, scale = 15)
    @DecimalMin(value = "0", message = "{mensaje_renglon_credito_precioUnitario_negativo}")
    private BigDecimal precioUnitario;
        
    @Column(precision = 25, scale = 15)
    @DecimalMin(value = "0", message = "{mensaje_renglon_credito_gananciaPorcentaje_negativa}")
    private BigDecimal gananciaPorcentaje;
        
    @Column(precision = 25, scale = 15)
    @DecimalMin(value = "0", message = "{mensaje_renglon_credito_gananciaNeto_negativa}")
    private BigDecimal gananciaNeto;
    
    @Column(precision = 25, scale = 15)
    @DecimalMin(value = "0", message = "{mensaje_renglon_credito_importe_negativo}")
    private BigDecimal importe; 
    
    @Column(precision = 25, scale = 15)
    @DecimalMin(value = "0", message = "{mensaje_renglon_credito_descuentoPorcentaje_negativo}")
    private BigDecimal descuentoPorcentaje;
    
    @Column(precision = 25, scale = 15)
    @DecimalMin(value = "0", message = "{mensaje_renglon_credito_descuentoNeto_negativo}")
    private BigDecimal descuentoNeto; 
    
    @Column(precision = 25, scale = 15)
    @DecimalMin(value = "0", message = "{mensaje_renglon_credito_importeBruto_negativo}")
    private BigDecimal importeBruto;
        
    @Column(precision = 25, scale = 15)
    @DecimalMin(value = "0", message = "{mensaje_renglon_credito_ivaPorcentaje_negativo}")
    private BigDecimal ivaPorcentaje;
    
    @Column(precision = 25, scale = 15)
    @DecimalMin(value = "0", message = "{mensaje_renglon_credito_ivaNeto_negativo}")
    private BigDecimal ivaNeto;
    
    @Column(precision = 25, scale = 15)
    @DecimalMin(value = "0", message = "{mensaje_renglon_credito_importeNeto_negativo}")
    private BigDecimal importeNeto; 

}
