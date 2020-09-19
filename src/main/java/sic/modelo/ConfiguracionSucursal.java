package sic.modelo;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.persistence.*;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import sic.controller.Views;

@Entity
@Table(name = "configuracionsucursal")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "idConfiguracionSucursal")
@ToString(exclude = "certificadoAfip")
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

//  @ManyToOne
//  @JoinColumn(name = "idSucursal", referencedColumnName = "idSucursal")
//  private Sucursal sucursal;

//  @JsonGetter("idSucursal")
//  public Long getIdSucursal() {
//    return sucursal.getIdSucursal();
//  }
//
//  @JsonGetter("nombreSucursal")
//  public String getNombreSucursal() {
//    return sucursal.getNombre();
//  }

  @JsonGetter("existeCertificado")
  public boolean isExisteCertificado() {
    return (certificadoAfip != null);
  }
}
