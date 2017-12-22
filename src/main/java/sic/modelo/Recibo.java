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

@Entity
@Table(name = "recibo")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"nroRecibo", "empresa", "fecha"})
@JsonIgnoreProperties({"formaDePago", "empresa", "cliente", "usuario"})
public class Recibo implements Serializable {
    
    @Id
    @GeneratedValue
    private Long idRecibo;
    
    private long nroRecibo;
    
    private boolean eliminado;
    
    @Column(nullable = false)
    private String observacion;
    
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
    @JoinColumn(name = "id_Usuario", referencedColumnName = "id_Usuario")
    private Usuario usuario;
    
    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fecha;
    
    private double monto;
    
    private double saldoSobrante;
    
    @JsonGetter("formaDePago")
    public String getNombreFormaDePago() {
        return formaDePago.getNombre();
    }
    
    @JsonGetter("nombreEmpresa")
    public String getNombreEmpresa() {
        return empresa.getNombre();
    }
    
    @JsonGetter("razonSocialCliente")
    public String getRazonSocialCliente() {
        return cliente.getRazonSocial();
    }
    
    @JsonGetter("nombreUsuario")
    public String getNombreUsuario() {
        return usuario.getNombre();
    }
    
}
