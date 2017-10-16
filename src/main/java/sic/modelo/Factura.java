package sic.modelo;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
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
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "factura")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@EqualsAndHashCode(of = {"fecha", "tipoComprobante", "numSerie", "numFactura", "empresa"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id_Factura", scope = Factura.class)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
  @Type(value = FacturaCompra.class), 
  @Type(value = FacturaVenta.class) 
})
public abstract class Factura implements Serializable {

    // bug: https://jira.spring.io/browse/DATAREST-304
    @JsonGetter(value = "type")
    public String getType() {
        return this.getClass().getSimpleName();
    }
    
    @Id
    @GeneratedValue
    private long id_Factura;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fecha;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TipoDeComprobante tipoComprobante;

    private long numSerie;

    private long numFactura;

    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaVencimiento;
    
    @ManyToOne
    @JoinColumn(name = "id_Pedido", referencedColumnName = "id_Pedido")    
    private Pedido pedido;

    @ManyToOne
    @JoinColumn(name = "id_Transportista", referencedColumnName = "id_Transportista")
    private Transportista transportista;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "id_Factura")
    @JsonProperty(access = Access.WRITE_ONLY)
    private List<RenglonFactura> renglones;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "factura")
    private List<Pago> pagos;

    private double subTotal;
    private double recargo_porcentaje;
    private double recargo_neto;
    private double descuento_porcentaje;
    private double descuento_neto;
    private double subTotal_bruto;
    private double iva_105_neto;
    private double iva_21_neto;
    private double impuestoInterno_neto;
    private double total;

    @Column(nullable = false)
    private String observaciones;

    private boolean pagada;

    @ManyToOne
    @JoinColumn(name = "id_Empresa", referencedColumnName = "id_Empresa")    
    private Empresa empresa;

    private boolean eliminada;
    
    private long CAE;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date vencimientoCAE;
    
    private long numSerieAfip;

    private long numFacturaAfip;

    public Factura() {}
        
    public Factura(long id_Factura, Date fecha, TipoDeComprobante tipoComprobante, long numSerie,
            long numFactura, Date fechaVencimiento, Pedido pedido, Transportista transportista,
            List<RenglonFactura> renglones, List<Pago> pagos, double subTotal,
            double recargo_porcentaje, double recargo_neto, double descuento_porcentaje,
            double descuento_neto, double subTotal_bruto, double iva_105_neto, double iva_21_neto,
            double impuestoInterno_neto, double total, String observaciones, boolean pagada,
            Empresa empresa, boolean eliminada, long CAE, Date vencimientoCAE, long numSerieAfip,
            long numFacturaAfip) {
        this.id_Factura = id_Factura;
        this.fecha = fecha;
        this.tipoComprobante = tipoComprobante;
        this.numSerie = numSerie;
        this.numFactura = numFactura;
        this.fechaVencimiento = fechaVencimiento;
        this.pedido = pedido;
        this.transportista = transportista;
        this.renglones = renglones;
        this.pagos = pagos;
        this.subTotal = subTotal;
        this.recargo_porcentaje = recargo_porcentaje;
        this.recargo_neto = recargo_neto;
        this.descuento_porcentaje = descuento_porcentaje;
        this.descuento_neto = descuento_neto;
        this.subTotal_bruto = subTotal_bruto;
        this.iva_105_neto = iva_105_neto;
        this.iva_21_neto = iva_21_neto;
        this.impuestoInterno_neto = impuestoInterno_neto;
        this.total = total;
        this.observaciones = observaciones;
        this.pagada = pagada;
        this.empresa = empresa;
        this.eliminada = eliminada;
        this.CAE = CAE;
        this.vencimientoCAE = vencimientoCAE;
        this.numSerieAfip = numSerieAfip;
        this.numFacturaAfip = numFacturaAfip;
    }
}
