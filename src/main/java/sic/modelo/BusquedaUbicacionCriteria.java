package sic.modelo;

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

  private Long idLocalidad;
  private Long idProvincia;
  private String descripcion;
  private String codigoPostal;
  private Pageable pageable;
}
