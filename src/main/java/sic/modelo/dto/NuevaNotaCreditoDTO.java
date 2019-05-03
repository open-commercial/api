package sic.modelo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sic.modelo.TipoDeComprobante;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NuevaNotaCreditoDTO {

  private Long idCliente;
  private Long idProveedor;
  private Long idFactura;
  private BigDecimal[] cantidades;
  private Long[] idsRenglonesFactura;
  private BigDecimal monto;
  private TipoDeComprobante tipo;
  private String detalle;
}
