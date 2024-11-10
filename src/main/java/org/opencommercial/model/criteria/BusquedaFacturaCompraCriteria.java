package org.opencommercial.model.criteria;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.opencommercial.model.TipoDeComprobante;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BusquedaFacturaCompraCriteria {

  private LocalDateTime fechaAltaDesde;
  private LocalDateTime fechaAltaHasta;
  private LocalDateTime fechaFacturaDesde;
  private LocalDateTime fechaFacturaHasta;
  private Long idProveedor;
  private Long numSerie;
  private Long numFactura;
  private TipoDeComprobante tipoComprobante;
  private Long idProducto;
  private Long idSucursal;
  private Integer pagina;
  private String ordenarPor;
  private String sentido;
}
