package sic.modelo;

import java.io.Serializable;
import java.math.BigDecimal;
import javax.persistence.*;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import sic.controller.Views;

import javax.validation.constraints.NotEmpty;

@Entity
@Table(name = "renglonnotadebito")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonView(Views.Comprador.class)
public class RenglonNotaDebito implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
