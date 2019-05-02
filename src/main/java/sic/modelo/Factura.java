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
import javax.validation.constraints.DecimalMin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "factura")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"fecha", "tipoComprobante", "numSerie", "numFactura", "empresa"})
@ToString(exclude = {"renglones"})
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

    @ManyToOne
    @JoinColumn(name = "id_Usuario", referencedColumnName = "id_Usuario")
    private Usuario usuario;

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

    @Column(precision = 25, scale = 15)
    private BigDecimal subTotal;
    
    @Column(precision = 25, scale = 15)
    private BigDecimal recargoPorcentaje;
    
    @Column(precision = 25, scale = 15)
    private BigDecimal recargoNeto;
    
    @Column(precision = 25, scale = 15)
    private BigDecimal descuentoPorcentaje;
    
    @Column(precision = 25, scale = 15)
    private BigDecimal descuentoNeto;
    
    @Column(precision = 25, scale = 15)
    private BigDecimal subTotalBruto;
    
    @Column(precision = 25, scale = 15)
    private BigDecimal iva105Neto;
    
    @Column(precision = 25, scale = 15)
    private BigDecimal iva21Neto;
    
    @Column(precision = 25, scale = 15)
    private BigDecimal impuestoInternoNeto;
    
    @Column(precision = 25, scale = 15)
    private BigDecimal total;

    @Column(nullable = false)
    private String observaciones;

    @Column(precision = 25, scale = 15)
    @DecimalMin(value = "0", message = "{mensaje_cantidad_de_productos_negativa}", inclusive = false)
    private BigDecimal cantidadArticulos;

    @ManyToOne
    @JoinColumn(name = "id_Empresa", referencedColumnName = "id_Empresa")    
    private Empresa empresa;

    private boolean eliminada;
    
    private long CAE;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date vencimientoCAE;
    
    private long numSerieAfip;

    private long numFacturaAfip;
    
    @JsonGetter("nombreTransportista")
    public String getNombreTransportista() {
        return transportista.getNombre();
    }    
    
    @JsonGetter("nombreEmpresa")
    public String getNombreEmpresa() {
        return empresa.getNombre();
    }

    @JsonGetter("nombreUsuario")
    public String getNombreUsuario() {
        return usuario.getNombre() + " " + usuario.getApellido() + " (" + usuario.getUsername() + ")";
    }
    
}
