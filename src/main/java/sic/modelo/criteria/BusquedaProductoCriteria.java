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
public class BusquedaProductoCriteria {

  private boolean buscarPorCodigo;
  private String codigo;
  private boolean buscarPorDescripcion;
  private String descripcion;
  private boolean buscarPorRubro;
  private Long idRubro;
  private boolean buscarPorProveedor;
  private Long idProveedor;
  private boolean listarSoloFaltantes;
  private boolean listarSoloEnStock;
  private boolean buscaPorVisibilidad;
  private Boolean publico;
  private boolean buscaPorOferta;
  private Boolean oferta;
  private Pageable pageable;
}
