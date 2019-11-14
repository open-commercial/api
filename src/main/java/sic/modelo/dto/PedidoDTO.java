package sic.modelo.dto;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.*;
import sic.modelo.EstadoPedido;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"renglones", "cantidadArticulos"})
@EqualsAndHashCode(of = {"nroPedido", "nombreSucursal"})
@JsonIdentityInfo(
    generator = ObjectIdGenerators.PropertyGenerator.class,
    property = "idPedido",
    scope = PedidoDTO.class)
@Builder
public class PedidoDTO implements Serializable {

  private long idPedido;
  private long nroPedido;
  private LocalDateTime fecha;
  private LocalDate fechaVencimiento;
  private String observaciones;
  private String nombreSucursal;
  private String detalleEnvio;
  private boolean eliminado;
  private String nombreFiscalCliente;
  private String nombreUsuario;
  private List<RenglonPedidoDTO> renglones;
  private BigDecimal subTotal;
  private BigDecimal recargoPorcentaje;
  private BigDecimal recargoNeto;
  private BigDecimal descuentoPorcentaje;
  private BigDecimal descuentoNeto;
  private BigDecimal totalEstimado;
  private BigDecimal totalActual;
  private EstadoPedido estado;
  private BigDecimal cantidadArticulos;
}
