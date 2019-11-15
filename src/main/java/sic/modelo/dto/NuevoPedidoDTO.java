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
public class NuevoPedidoDTO {

  private String observaciones;
  private Long idSucursal;
  private TipoDeEnvio tipoDeEnvio;
  private Long idUsuario;
  private Long idCliente;
  private List<NuevoRenglonPedidoDTO> renglones;
  private BigDecimal recargoPorcentaje;
  private BigDecimal descuentoPorcentaje;
}
