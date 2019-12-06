package sic.modelo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sic.modelo.TipoDeEnvio;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DetallePedidoDTO {

  private Long idPedido;
  private Long idSucursal;
  private String observaciones;
  private Long idCliente;
  private TipoDeEnvio tipoDeEnvio;
  private List<NuevoRenglonPedidoDTO> renglones;
  private BigDecimal recargoPorcentaje;
  private BigDecimal descuentoPorcentaje;
}
