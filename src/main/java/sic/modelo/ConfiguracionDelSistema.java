package sic.modelo;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.validator.constraints.Email;

@Entity
@Table(name = "configuraciondelsistema")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"id_ConfiguracionDelSistema", "empresa"})
@ToString(exclude = "certificadoAfip")
@JsonIgnoreProperties({"tokenWSAA", "signTokenWSAA", "fechaGeneracionTokenWSAA", "fechaVencimientoTokenWSAA", "empresa"})
public class ConfiguracionDelSistema implements Serializable {

    @Id
    @GeneratedValue
    private long id_ConfiguracionDelSistema;

    private boolean usarFacturaVentaPreImpresa;

    private int cantidadMaximaDeRenglonesEnFactura;

    private boolean facturaElectronicaHabilitada;

    @Lob
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private byte[] certificadoAfip;

    private String firmanteCertificadoAfip;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String passwordCertificadoAfip;

    private int nroPuntoDeVentaAfip;

    @Column(length = 1000)
    private String tokenWSAA;

    private String signTokenWSAA;

    private boolean emailSenderHabilitado;

    @Email(message = "{mensaje_cds_email_invalido}")
    private String emailUsername;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String emailPassword;

    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaGeneracionTokenWSAA;

    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaVencimientoTokenWSAA;

    @ManyToOne
    @JoinColumn(name = "id_Empresa", referencedColumnName = "id_Empresa")
    private Empresa empresa;

    @JsonGetter("idEmpresa")
    public Long getIdEmpresa() {
        return empresa.getId_Empresa();
    }

    @JsonGetter("nombreEmpresa")
    public String getNombreEmpresa() {
        return empresa.getNombre();
    }

    @JsonGetter("existeCertificado")
    public boolean isExisteCertificado() {
        return (certificadoAfip != null);
    }

}
