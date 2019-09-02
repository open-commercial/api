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
public class BusquedaUbicacionCriteria {

  private boolean buscaPorLocalidad;
  private Long idLocalidad;
  private boolean buscaPorProvincia;
  private Long idProvincia;
  private boolean buscarPorDescripcion;
  private String descripcion;
  private boolean buscaPorCodigoPostal;
  private String codigoPostal;
  private Pageable pageable;
}
