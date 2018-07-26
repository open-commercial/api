package sic.modelo;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.querydsl.core.annotations.QueryInit;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "cliente")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"razonSocial", "idFiscal", "empresa"})
@ToString
@JsonIgnoreProperties({"condicionIVA", "localidad", "empresa", "viajante", "credencial", "eliminado"})
public class Cliente implements Serializable {

    @Id
    @GeneratedValue
    private long id_Cliente;

    private long nroCliente;

    @Column(nullable = false)
    private String razonSocial;

    private String nombreFantasia;

    private String direccion;

    @ManyToOne
    @JoinColumn(name = "id_CondicionIVA", referencedColumnName = "id_CondicionIVA")
    private CondicionIVA condicionIVA;

    private String idFiscal;

    private String email;

    private String telPrimario;

    private String telSecundario;

    @ManyToOne
    @JoinColumn(name = "id_Localidad", referencedColumnName = "id_Localidad")
    @QueryInit("provincia.pais")
    private Localidad localidad;

    private String contacto;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaAlta;

    @ManyToOne
    @JoinColumn(name = "id_Empresa", referencedColumnName = "id_Empresa")
    private Empresa empresa;

    @ManyToOne
    @JoinColumn(name = "id_Usuario_Viajante", referencedColumnName = "id_Usuario")
    private Usuario viajante;

    @OneToOne
    @JoinColumn(name = "id_Usuario_Credencial", referencedColumnName = "id_Usuario")
    private Usuario credencial;

    private boolean eliminado;

    private boolean predeterminado;

    @Transient
    private BigDecimal saldoCuentaCorriente;
    
    @Transient 
    private Date fechaUltimoMovimiento;

    @JsonGetter("idCondicionIVA")
    public Long getIdCondicionIVA() {
        return condicionIVA.getId_CondicionIVA();
    }

    @JsonGetter("nombreCondicionIVA")
    public String getNombreCondicionIVA() {
        return condicionIVA.getNombre();
    }

    @JsonGetter("idLocalidad")
    public Long getIdLocalidad() {
        return localidad.getId_Localidad();
    }

    @JsonGetter("nombreLocalidad")
    public String getNombreLocalidad() {
        return localidad.getNombre();
    }

    @JsonGetter("idProvincia")
    public Long getIdProvincia() {
        return localidad.getProvincia().getId_Provincia();
    }

    @JsonGetter("nombreProvincia")
    public String getNombreProvincia() {
        return localidad.getProvincia().getNombre();
    }

    @JsonGetter("idPais")
    public Long getIdPais() {
        return localidad.getProvincia().getPais().getId_Pais();
    }

    @JsonGetter("nombrePais")
    public String getNombrePais() {
        return localidad.getProvincia().getPais().getNombre();
    }

    @JsonGetter("idEmpresa")
    public Long getIdEmpresa() {
        return empresa.getId_Empresa();
    }

    @JsonGetter("nombreEmpresa")
    public String getNombreEmpresa() {
        return empresa.getNombre();
    }

    @JsonGetter("idViajante")
    public Long getIdViajante() {
        if (viajante != null) {
            return viajante.getId_Usuario();
        } else {
            return null;
        }
    }

    @JsonGetter("nombreViajante")
    public String getNombreViajante() {
        if (viajante != null) {
            return viajante.getNombre() + " " + viajante.getApellido() + " (" + viajante.getUsername() + ")";
        } else {
            return null;
        }
    }

    @JsonGetter("idCredencial")
    public Long getIdCredencial() {
        if (credencial != null) {
            return credencial.getId_Usuario();
        } else {
            return null;
        }
    }

    @JsonGetter("nombreCredencial")
    public String getNombreCredencial() {
        if (credencial != null) {
            return credencial.getNombre() + " " + credencial.getApellido() + " (" + credencial.getUsername() + ")";
        } else {
            return null;
        }
    }
}