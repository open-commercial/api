package org.opencommercial.integration.model;

import lombok.*;
import org.opencommercial.model.EstadoPedido;
import org.opencommercial.model.TipoDeEnvio;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"renglones", "cantidadArticulos"})
@EqualsAndHashCode(of = {"nroPedido", "nombreSucursal"})
@Builder
public class PedidoTest {

  private long idPedido;
  private long nroPedido;
  private LocalDateTime fecha;
  private LocalDateTime fechaVencimiento;
  private String observaciones;
  private long idSucursal;
  private String nombreSucursal;
  private String detalleEnvio;
  private TipoDeEnvio tipoDeEnvio;
  private boolean eliminado;
  private ClienteTest cliente;
  private String nombreUsuario;
  private List<RenglonPedidoTest> renglones;
  private BigDecimal subTotal;
  private BigDecimal recargoPorcentaje;
  private BigDecimal recargoNeto;
  private BigDecimal descuentoPorcentaje;
  private BigDecimal descuentoNeto;
  private BigDecimal total;
  private EstadoPedido estado;
  private BigDecimal cantidadArticulos;
}
