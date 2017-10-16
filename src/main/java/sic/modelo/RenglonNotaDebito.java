package sic.modelo;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "renglonnotadebito")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RenglonNotaDebito implements Serializable {
    
    @Id
    @GeneratedValue
    private long idRenglonNotaDebito;
    
    @Column(nullable = false)
    private String descripcion;
    
    @Column(nullable = false)
    private double monto; 
    
    @Column(nullable = false)
    private double importeBruto; 
    
    @Column(nullable = false)
    private double ivaPorcentaje;
    
    @Column(nullable = false)
    private double ivaNeto;
    
    @Column(nullable = false)
    private double importeNeto;

}
