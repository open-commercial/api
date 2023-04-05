package sic.modelo.criteria;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sic.modelo.SortingProducto;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BusquedaProductoCriteria {

  private String codigo;
  private String descripcion;
  private Long idRubro;
  private Long idProveedor;
  private boolean listarSoloFaltantes;
  private boolean listarSoloEnStock;
  private Boolean listarSoloParaCatalogo;
  private Boolean publico;
  private Boolean oferta;
  private Integer pagina;
  private List<String> ordenarPor;
  private String sentido;
}
