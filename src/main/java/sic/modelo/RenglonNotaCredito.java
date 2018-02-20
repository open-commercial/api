package sic.modelo;

import java.io.Serializable;
import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "renglonnotacredito")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"idProductoItem", "codigoItem"})
@ToString
public class RenglonNotaCredito implements Serializable {
    
    @Id
    @GeneratedValue
    private long idRenglonNotaCredito;
    
    private long idProductoItem;

    private String codigoItem;

    @Column(nullable = false)
    private String descripcionItem;

    private String medidaItem;
        
    @Column(precision = 25, scale = 15)
    private BigDecimal cantidad;
    
    @Column(precision = 25, scale = 15)
    private BigDecimal precioUnitario;
        
    @Column(precision = 25, scale = 15)
    private BigDecimal gananciaPorcentaje;
        
    @Column(precision = 25, scale = 15)
    private BigDecimal gananciaNeto;
    
    @Column(precision = 25, scale = 15)
    private BigDecimal importe; 
    
    @Column(precision = 25, scale = 15)
    private BigDecimal descuentoPorcentaje;
    
    @Column(precision = 25, scale = 15)
    private BigDecimal descuentoNeto; 
    
    @Column(precision = 25, scale = 15)
    private BigDecimal importeBruto;
        
    @Column(precision = 25, scale = 15)
    private BigDecimal ivaPorcentaje;
    
    @Column(precision = 25, scale = 15)
    private BigDecimal ivaNeto;
    
    @Column(precision = 25, scale = 15)
    private BigDecimal importeNeto; 

}
