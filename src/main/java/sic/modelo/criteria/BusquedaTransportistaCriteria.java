package sic.modelo.criteria;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BusquedaTransportistaCriteria {

  private String nombre;
  private Long idProvincia;
  private Long idLocalidad;
  private Long idEmpresa;
}
