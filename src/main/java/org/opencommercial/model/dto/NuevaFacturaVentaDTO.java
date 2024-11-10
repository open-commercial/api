package org.opencommercial.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.opencommercial.model.TipoDeComprobante;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NuevaFacturaVentaDTO {

  private Long idSucursal;
  private Long idCliente;
  private Long idTransportista;
  private LocalDate fechaVencimiento;
  private TipoDeComprobante tipoDeComprobante;
  private String observaciones;
  private boolean[] renglonMarcado;
  private Long[] idsFormaDePago;
  private BigDecimal[] montos;
  private int[] indices;
  private BigDecimal recargoPorcentaje;
  private BigDecimal descuentoPorcentaje;
}
