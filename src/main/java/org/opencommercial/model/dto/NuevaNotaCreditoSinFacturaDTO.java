package org.opencommercial.model.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.opencommercial.model.TipoDeComprobante;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NuevaNotaCreditoSinFacturaDTO {

  private Long idCliente;
  private Long idProveedor;
  private long idSucursal;
  private BigDecimal monto;
  private TipoDeComprobante tipo;
  private String detalle;
  private String motivo;
  private DetalleCompraDTO detalleCompra;
}
