package sic.modelo.criteria;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Pageable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BusquedaLocalidadCriteria {

  private boolean buscaPorNombre;
  private String nombre;
  private boolean buscaPorCodigoPostal;
  private String codigoPostal;
  private boolean buscaPorNombreProvincia;
  private String nombreProvincia;
  private boolean buscaPorEnvio;
  private Boolean envioGratuito;
  private Pageable pageable;
}
