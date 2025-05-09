package org.opencommercial.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.opencommercial.config.Views;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "configuracionsucursal")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "idConfiguracionSucursal")
@ToString(exclude = {
        "certificadoAfip",
        "passwordCertificadoAfip",
        "tokenWSAA",
        "signTokenWSAA",
        "fechaGeneracionTokenWSAA",
        "fechaVencimientoTokenWSAA"
})
@JsonIgnoreProperties({
  "tokenWSAA",
  "signTokenWSAA",
  "fechaGeneracionTokenWSAA",
  "fechaVencimientoTokenWSAA",
  "sucursal"
})
@JsonView(Views.Comprador.class)
public class ConfiguracionSucursal implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long idConfiguracionSucursal;

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

  private boolean puntoDeRetiro;

  private boolean predeterminada;

  private boolean comparteStock;

  @NotNull(message = "{mensaje_cds_sin_vencimiento_largo}")
  @DecimalMin(value = "1", message = "{mensaje_cds_valor_no_valido}")
  private long vencimientoLargo;

  @NotNull(message = "{mensaje_cds_sin_vencimiento_corto}")
  @DecimalMin(value = "1", message = "{mensaje_cds_valor_no_valido}")
  private long vencimientoCorto;

  private LocalDateTime fechaGeneracionTokenWSAA;

  private LocalDateTime fechaVencimientoTokenWSAA;

  @JsonGetter("existeCertificado")
  public boolean isExisteCertificado() {
    return (certificadoAfip != null);
  }
}
