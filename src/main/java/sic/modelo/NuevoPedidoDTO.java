package sic.modelo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class NuevoPedidoDTO {

  private Date fechaVencimiento;
  private String observaciones;
  private List<RenglonPedido> renglones;
  private BigDecimal subTotal;
  private BigDecimal recargoPorcentaje;
  private BigDecimal recargoNeto;
  private BigDecimal descuentoPorcentaje;
  private BigDecimal descuentoNeto;
  private BigDecimal total;

}
