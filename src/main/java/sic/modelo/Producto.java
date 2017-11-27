package sic.modelo;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "producto")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"descripcion", "empresa"})
@ToString
@JsonIgnoreProperties({"medida", "rubro", "proveedor", "empresa"})
public class Producto implements Serializable {

    @Id
    @GeneratedValue
    private long id_Producto;

    @Column(nullable = false)
    private String codigo;

    @Column(nullable = false)
    private String descripcion;

    private double cantidad;

    private double cantMinima;
    
    private double ventaMinima;

    @ManyToOne
    @JoinColumn(name = "id_Medida", referencedColumnName = "id_Medida")
    private Medida medida;
        
    private double precioCosto;
    private double ganancia_porcentaje;
    private double ganancia_neto;
    private double precioVentaPublico;
    private double iva_porcentaje;
    private double iva_neto;
    private double impuestoInterno_porcentaje;
    private double impuestoInterno_neto;
    private double precioLista;

    @ManyToOne
    @JoinColumn(name = "id_Rubro", referencedColumnName = "id_Rubro")
    private Rubro rubro;
        
    private boolean ilimitado;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaUltimaModificacion;

    @Column(nullable = false)
    private String estanteria;

    @Column(nullable = false)
    private String estante;
    
    @ManyToOne
    @JoinColumn(name = "id_Proveedor", referencedColumnName = "id_Proveedor")
    private Proveedor proveedor;
        
    @Column(nullable = false)
    private String nota;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaAlta;

    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaVencimiento;

    @ManyToOne
    @JoinColumn(name = "id_Empresa", referencedColumnName = "id_Empresa")
    private Empresa empresa;
    
    private boolean eliminado;

    @JsonGetter("nombreMedida")
    public String getNombreMedida() {
        return medida.getNombre();
    }    
    
    @JsonGetter("nombreRubro")
    public String getNombreRubro() {
        return rubro.getNombre();
    }    
    
    @JsonGetter("razonSocialProveedor")
    public String getRazonSocialProveedor() {
        return proveedor.getRazonSocial();
    }    
    
    @JsonGetter("nombreEmpresa")
    public String getNombreEmpresa() {
        return empresa.getNombre();
    }

}