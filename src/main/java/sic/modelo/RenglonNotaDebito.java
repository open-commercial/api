package sic.modelo;

import java.io.Serializable;
import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.validator.constraints.NotEmpty;

@Entity
@Table(name = "renglonnotadebito")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RenglonNotaDebito implements Serializable {
    
    @Id
    @GeneratedValue
    private long idRenglonNotaDebito;
    
    @Column(nullable = false)
    @NotEmpty(message = "{mensaje_renglon_debito_vacio_descripcion}")
    @NotNull(message = "{mensaje_renglon_debito_vacio_descripcion}")
    private String descripcion;
     
    @Column(precision = 25, scale = 15)
    @DecimalMin(value = "0", message = "{mensaje_renglon_debito_monto_negativo}")
    private BigDecimal monto; 
    
    @Column(precision = 25, scale = 15)
    @DecimalMin(value = "0", message = "{mensaje_renglon_debito_importeBruto_negativo}")
    private BigDecimal importeBruto; 
    
    @Column(precision = 25, scale = 15)
    @DecimalMin(value = "0", message = "{mensaje_renglon_debito_IVAPorcentaje_negativo}")
    private BigDecimal ivaPorcentaje;
    
    @Column(precision = 25, scale = 15)
    @DecimalMin(value = "0", message = "{mensaje_renglon_debito_IVANeto_negativo}")
    private BigDecimal ivaNeto;
    
    @Column(precision = 25, scale = 15)
    @DecimalMin(value = "0", message = "{mensaje_renglon_debito_importeNeto_negativo}")
    private BigDecimal importeNeto;

}
