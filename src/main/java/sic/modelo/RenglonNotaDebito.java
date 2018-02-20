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
import lombok.NoArgsConstructor;
import lombok.ToString;

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
    private String descripcion;
     
    @Column(precision = 25, scale = 15)
    private BigDecimal monto; 
    
    @Column(precision = 25, scale = 15)
    private BigDecimal importeBruto; 
    
    @Column(precision = 25, scale = 15)
    private BigDecimal ivaPorcentaje;
    
    @Column(precision = 25, scale = 15)
    private BigDecimal ivaNeto;
    
    @Column(precision = 25, scale = 15)
    private BigDecimal importeNeto;

}
