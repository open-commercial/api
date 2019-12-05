package sic.modelo.dto;

import lombok.Data;
import sic.modelo.TipoDeEnvio;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ActualizarPedidoDTO {

  private long idPedido;
  Long idSucursal;
  //Long idUsuario;
  //Long idCliente;
  TipoDeEnvio tipoDeEnvio;
  //private long nroPedido;
  //private LocalDateTime fecha;
  private String observaciones;
  private List<NuevoRenglonPedidoDTO> renglones;
  //private BigDecimal subTotal;
  private BigDecimal recargoPorcentaje;
  //private BigDecimal recargoNeto;
  private BigDecimal descuentoPorcentaje;
  //private BigDecimal descuentoNeto;
  //private BigDecimal totalEstimado;
  //private BigDecimal totalActual;
  //private EstadoPedido estado;
  //private BigDecimal cantidadArticulos;
}
