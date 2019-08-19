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
public class NuevaNotaCreditoSinFacturaDTO {

  private Long idCliente;
  private Long idProveedor;
  private long idSucursal;
  private BigDecimal monto;
  private TipoDeComprobante tipo;
  private String detalle;
  private String motivo;
}
