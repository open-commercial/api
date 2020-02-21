package sic.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"idLocalidad", "idProvincia", "nombreProvincia"})
public class Localidad {

  private long idLocalidad;
  private String nombre;
  private String codigoPostal;
  private Long idProvincia;
  private String nombreProvincia;
  private boolean envioGratuito;
  private BigDecimal costoEnvio;
}
