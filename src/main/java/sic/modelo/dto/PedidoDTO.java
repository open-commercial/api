package sic.modelo.dto;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import lombok.*;
import sic.modelo.DetalleEnvio;
import sic.modelo.EstadoPedido;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "renglones")
@EqualsAndHashCode(of = {"nroPedido", "nombreEmpresa"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id_Pedido", scope = PedidoDTO.class)
@Builder
public class PedidoDTO implements Serializable {

  private long id_Pedido;
  private long nroPedido;
  private Date fecha;
  private Date fechaVencimiento;
  private String observaciones;
  private String nombreEmpresa;
  private DetalleEnvio detalleEnvio;
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

}
