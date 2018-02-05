package sic.modelo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "ajustecuentacorriente")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"numAjuste", "numSerie", "empresa"})
@ToString
public class AjusteCuentaCorriente implements Serializable {

    @Id
    @GeneratedValue
    private long idAjusteCuentaCorriente;
    
    private long numSerie;

    private long numAjuste;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fecha;

    @Column(nullable = false)
    private String concepto;

    @ManyToOne
    @JoinColumn(name = "id_Empresa", referencedColumnName = "id_Empresa")
    private Empresa empresa;

    @OneToOne
    @JoinColumn(name = "id_Usuario", referencedColumnName = "id_Usuario")
    private Usuario usuario;
    
    @ManyToOne
    @JoinColumn(name = "id_Cliente", referencedColumnName = "id_Cliente")
    private Cliente cliente;
    
    @OneToOne
    @JoinColumn(name = "idNota", referencedColumnName = "idNota")
    private Nota notaDebito;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TipoDeComprobante tipoComprobante;
    
    @Column(precision = 18, scale = 15)
    private BigDecimal monto;
    
    private boolean eliminado;     
    
}
