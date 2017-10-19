package sic.modelo;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import java.io.Serializable;
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
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "nota")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@EqualsAndHashCode(of = {"fecha", "tipoComprobante", "serie", "nroNota", "empresa", "cliente"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "idNota", scope = Nota.class)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = NotaCredito.class), 
  @JsonSubTypes.Type(value = NotaDebito.class) 
})
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
    @JoinColumn(name = "id_Cliente", referencedColumnName = "id_Cliente")
    private Cliente cliente;
    
    @ManyToOne
    @JoinColumn(name = "id_Usuario", referencedColumnName = "id_Usuario")
    private Usuario usuario;
    
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "id_Factura")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private FacturaVenta facturaVenta;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "notaDebito")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<Pago> pagos;
    
    @Column(nullable = false)
    private String motivo;
    
    @Column(nullable = false)
    private double subTotalBruto; 

    @Column(nullable = false)
    private double iva21Neto;
            
    @Column(nullable = false)        
    private double iva105Neto;
    
    @Column(nullable = false)
    private double total;
    
    private long CAE;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date vencimientoCAE;
    
    private long numSerieAfip;

    private long numNotaAfip;
    
    public Nota() {}
    
    public Nota(long idNota, long serie, FacturaVenta facturaVenta, List<Pago> pagos, long nroNota, boolean eliminada, TipoDeComprobante tipoComprobante,
                Date fecha, Empresa empresa, Cliente cliente, Usuario usuario, String motivo,
                double subTotalBruto, double iva21Neto, double iva105Neto, double total, 
                long CAE, Date vencimientoCAE, long numSerieAfip, long numNotaAfip) {
        
        this.facturaVenta = facturaVenta;
        this.pagos = pagos;
        this.idNota = idNota;
        this.serie = serie;
        this.nroNota = nroNota;
        this.eliminada = eliminada;
        this.tipoComprobante = tipoComprobante;
        this.fecha = fecha;
        this.empresa = empresa;
        this.cliente = cliente;
        this.usuario = usuario;        
        this.motivo = motivo;
        this.subTotalBruto = subTotalBruto;
        this.iva21Neto = iva21Neto;
        this.iva105Neto = iva105Neto;
        this.total = total;
        this.CAE = CAE;
        this.vencimientoCAE = vencimientoCAE;
        this.numSerieAfip = numSerieAfip;
        this.numNotaAfip = numNotaAfip;
    }
    
}
