package org.opencommercial.model.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(exclude = {"idLocalidad", "idProvincia", "nombreProvincia"})
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LocalidadDTO {

  private long idLocalidad;
  private String nombre;
  private String codigoPostal;
  private Long idProvincia;
  private String nombreProvincia;
  private boolean envioGratuito;
  private BigDecimal costoEnvio;

}
