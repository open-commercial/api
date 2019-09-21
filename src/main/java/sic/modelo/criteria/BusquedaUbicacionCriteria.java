package sic.modelo.criteria;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BusquedaUbicacionCriteria {

  private Long idLocalidad;
  private Long idProvincia;
  private String descripcion;
  private String codigoPostal;
  private Integer pagina;
  private String ordenarPor;
  private String sentido;
}
