package org.opencommercial.model.embeddable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.opencommercial.model.CategoriaIVA;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Embeddable
public class ClienteEmbeddable implements Serializable {

  private String nroCliente;
  private String nombreFiscalCliente;
  private String nombreFantasiaCliente;

  @Enumerated(EnumType.STRING)
  private CategoriaIVA categoriaIVACliente;

  private Long idFiscalCliente;
  private String emailCliente;
  private String telefonoCliente;
  private String descripcionUbicacionCliente;
  private Double latitudUbicacionCliente;
  private Double longitudUbicacionCliente;
  private String calleUbicacionCliente;
  private Integer numeroUbicacionCliente;
  private String pisoUbicacionCliente;
  private String departamentoUbicacionCliente;
  private String nombreLocalidadCliente;
  private String codigoPostalLocalidadCliente;
  private BigDecimal costoEnvioLocalidadCliente;
  private String nombreProvinciaCliente;
}
