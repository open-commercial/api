package sic.entity.criteria;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sic.domain.EstadoPedido;
import sic.domain.TipoDeEnvio;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BusquedaPedidoCriteria {

  private LocalDateTime fechaDesde;
  private LocalDateTime fechaHasta;
  private Long idCliente;
  private Long idUsuario;
  private Long idViajante;
  private Long nroPedido;
  private EstadoPedido estadoPedido;
  private TipoDeEnvio tipoDeEnvio;
  private Long idProducto;
  private Long idSucursal;
  private Integer pagina;
  private String ordenarPor;
  private String sentido;
}
