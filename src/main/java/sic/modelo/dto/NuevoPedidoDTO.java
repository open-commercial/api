package sic.modelo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sic.modelo.TipoDeEnvio;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NuevoPedidoDTO {

  private LocalDateTime fechaVencimiento;
  private String observaciones;
  private Long idEmpresa;
  private Long idUsuario;
  private Long idCliente;
  private Long idSucursal;
  private TipoDeEnvio tipoDeEnvio;
  private List<RenglonPedidoDTO> renglones;
  private BigDecimal subTotal;
  private BigDecimal recargoPorcentaje;
  private BigDecimal recargoNeto;
  private BigDecimal descuentoPorcentaje;
  private BigDecimal descuentoNeto;
  private BigDecimal total;

}
