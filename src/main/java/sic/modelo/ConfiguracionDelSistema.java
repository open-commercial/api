package sic.modelo;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "configuraciondelsistema")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"id_ConfiguracionDelSistema", "empresa"})
@ToString(exclude = "certificadoAfip")
public class ConfiguracionDelSistema implements Serializable {

    @Id
    @GeneratedValue
    private long id_ConfiguracionDelSistema;

    private boolean usarFacturaVentaPreImpresa;

    private int cantidadMaximaDeRenglonesEnFactura;
    
    private boolean facturaElectronicaHabilitada;
    
    @Lob
    private byte[] certificadoAfip;
    
    private String firmanteCertificadoAfip;
    
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String passwordCertificadoAfip;
    
    private int nroPuntoDeVentaAfip;

    @Column(length = 1000)
    private String tokenWSAA;

    private String signTokenWSAA;

    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaGeneracionTokenWSAA;

    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaVencimientoTokenWSAA;

    @ManyToOne
    @JoinColumn(name = "id_Empresa", referencedColumnName = "id_Empresa")
    private Empresa empresa;

}
