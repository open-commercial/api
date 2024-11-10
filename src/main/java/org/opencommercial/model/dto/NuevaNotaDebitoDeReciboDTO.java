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
public class NuevaNotaDebitoDeReciboDTO {

  private long idRecibo;
  private BigDecimal gastoAdministrativo;
  private String motivo;
  private TipoDeComprobante tipoDeComprobante;
  private DetalleCompraDTO detalleCompra;

}
