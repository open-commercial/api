package sic.modelo.embeddable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Embeddable
public class LocalidadEmbeddable implements Serializable {

  private String nombreLocalidad;
  private String codigoPostal;
  private BigDecimal costoEnvio;
  private ProvinciaEmbeddable provincia;
}