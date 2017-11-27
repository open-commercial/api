package sic.modelo;

import java.io.Serializable;
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
        
    @Column(nullable = false)
    private double cantidad;
    
    private double precioUnitario;
        
    private double gananciaPorcentaje;
        
    private double gananciaNeto;
    
    @Column(nullable = false)
    private double importe; 
    
    private double descuentoPorcentaje;
    
    private double descuentoNeto; 
    
    @Column(nullable = false)
    private double importeBruto;
        
    @Column(nullable = false)
    private double ivaPorcentaje;
    
    @Column(nullable = false)
    private double ivaNeto;
    
    @Column(nullable = false)
    private double importeNeto; 

}
