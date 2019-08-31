package sic.modelo;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import sic.controller.Views;

import javax.validation.constraints.Email;

@Entity
@Table(name = "configuraciondelsistema")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id_ConfiguracionDelSistema")
@ToString(exclude = "certificadoAfip")
@JsonIgnoreProperties({"tokenWSAA", "signTokenWSAA", "fechaGeneracionTokenWSAA", "fechaVencimientoTokenWSAA"})
@JsonView(Views.Comprador.class)
public class ConfiguracionDelSistema implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    @JoinColumn(name = "idSucursal", referencedColumnName = "idSucursal")
    private Sucursal sucursal;

    @JsonGetter("idSucursal")
    public Long getIdSucursal() {
        return sucursal.getIdSucursal();
    }

    @JsonGetter("nombreSucursal")
    public String getNombreSucursal() {
        return sucursal.getNombre();
    }

    @JsonGetter("existeCertificado")
    public boolean isExisteCertificado() {
        return (certificadoAfip != null);
    }

}
