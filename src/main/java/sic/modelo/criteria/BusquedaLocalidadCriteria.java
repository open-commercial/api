package sic.modelo.criteria;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BusquedaLocalidadCriteria {

  private String nombre;
  private String codigoPostal;
  private String nombreProvincia;
  private Boolean envioGratuito;
  private Integer pagina;
  private String ordenarPor;
  private String sentido;
}
