package sic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sic.domain.TipoDeComprobante;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NuevaNotaDebitoSinReciboDTO {

  private Long idCliente;
  private Long idProveedor;
  private long idSucursal;
  private String motivo;
  private BigDecimal gastoAdministrativo;
  private TipoDeComprobante tipoDeComprobante;
  private DetalleCompraDTO detalleCompra;

}
