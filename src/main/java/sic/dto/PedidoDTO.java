package sic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sic.domain.TipoDeEnvio;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PedidoDTO {

  private Long idPedido;
  private long idSucursal;
  private String observaciones;
  private Long idCliente;
  private TipoDeEnvio tipoDeEnvio;
  private List<NuevoRenglonPedidoDTO> renglones;
  private Long[] idsFormaDePago;
  private BigDecimal[] montos;
  private BigDecimal recargoPorcentaje;
  private BigDecimal descuentoPorcentaje;
}
