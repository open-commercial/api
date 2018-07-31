package sic.modelo;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.math.BigDecimal;
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
@Table(name = "recibo")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(of = {"numSerie", "numRecibo", "empresa", "fecha"})
@JsonIgnoreProperties({"formaDePago", "empresa", "cliente", "usuario", "proveedor"})
public class Recibo implements Serializable {

    @Id
    @GeneratedValue
    private Long idRecibo;

    private long numSerie;

    private long numRecibo;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fecha;

    private boolean eliminado;

    @Column(nullable = false)
    private String concepto;

    @ManyToOne
    @JoinColumn(name = "id_FormaDePago", referencedColumnName = "id_FormaDePago")
    private FormaDePago formaDePago;

    @ManyToOne
    @JoinColumn(name = "id_Empresa", referencedColumnName = "id_Empresa")
    private Empresa empresa;

    @ManyToOne
    @JoinColumn(name = "id_Cliente", referencedColumnName = "id_Cliente")
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "id_Proveedor", referencedColumnName = "id_Proveedor")
    private Proveedor proveedor;

    @ManyToOne
    @JoinColumn(name = "id_Usuario", referencedColumnName = "id_Usuario")
    private Usuario usuario;

    @Column(precision = 25, scale = 15)
    private BigDecimal monto;

    @JsonGetter("nombreFormaDePago")
    public String getNombreFormaDePago() {
        return formaDePago.getNombre();
    }

    @JsonGetter("nombreEmpresa")
    public String getNombreEmpresa() {
        return empresa.getNombre();
    }

    @JsonGetter("razonSocialCliente")
    public String getRazonSocialCliente() {
        return (cliente != null) ? cliente.getRazonSocial() : "";
    }

    @JsonGetter("razonSocialProveedor")
    public String getRazonSocialProveedor() {
        return (proveedor != null) ? proveedor.getRazonSocial() : "";
    }
    
    @JsonGetter("nombreUsuario")
    public String getNombreUsuario() {
        return usuario.getNombre();
    }

}
