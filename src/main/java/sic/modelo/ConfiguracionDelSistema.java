package sic.modelo;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "configuraciondelsistema")
@Data
@AllArgsConstructor
@NoArgsConstructor
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

    @ManyToOne
    @JoinColumn(name = "id_Empresa", referencedColumnName = "id_Empresa")
    private Empresa empresa;

}
