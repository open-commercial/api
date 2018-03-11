package sic.modelo;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "nota")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"fecha", "tipoComprobante", "serie", "nroNota", "empresa"})
@ToString(exclude = "pagos")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "idNota", scope = Nota.class)
public abstract class Nota implements Serializable {
    
    @JsonGetter(value = "type")
    public String getType() {
        return this.getClass().getSimpleName();
    }
    
    @Id
    @GeneratedValue
    private Long idNota;
    
    @Column(nullable = false)
    private long serie;
    
    @Column(nullable = false)
    private long nroNota;
    
    private boolean eliminada;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TipoDeComprobante tipoComprobante;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fecha;
    
    @ManyToOne
    @JoinColumn(name = "id_Empresa", referencedColumnName = "id_Empresa")    
    private Empresa empresa;
    
    @ManyToOne
    @JoinColumn(name = "id_Usuario", referencedColumnName = "id_Usuario")
    private Usuario usuario;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "notaDebito")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<Pago> pagos;
    
    @Column(nullable = false)
    private String motivo;
    
    @Column(precision = 25, scale = 15)
    private BigDecimal subTotalBruto; 

    @Column(precision = 25, scale = 15)
    private BigDecimal iva21Neto;
            
    @Column(precision = 25, scale = 15)       
    private BigDecimal iva105Neto;
    
    @Column(precision = 25, scale = 15)
    private BigDecimal total;
    
    private long CAE;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date vencimientoCAE;
    
    private long numSerieAfip;

    private long numNotaAfip;
        
}