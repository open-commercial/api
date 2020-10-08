package sic.modelo.criteria;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BusquedaTraspasoCriteria {

  private LocalDateTime fechaDesde;
  private LocalDateTime fechaHasta;
  private String nroTraspaso;
  private Long nroPedido;
  private Long idSucursalOrigen;
  private Long idSucursalDestino;
  private Long idUsuario;
  private Long idProducto;
  private Integer pagina;
  private String ordenarPor;
  private String sentido;
}
