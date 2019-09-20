package sic.modelo.criteria;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BusquedaProductoCriteria {

  private String codigo;
  private String descripcion;
  private Long idRubro;
  private Long idProveedor;
  private long idEmpresa;
  private boolean listarSoloFaltantes;
  private boolean listarSoloEnStock;
  private Boolean publico;
  private Boolean destacado;
  private Integer pagina;
  private String ordenarPor;
  private String sentido;
}
