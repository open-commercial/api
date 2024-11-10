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
public class BusquedaFacturaVentaCriteria {

  private LocalDateTime fechaDesde;
  private LocalDateTime fechaHasta;
  private Long idCliente;
  private TipoDeComprobante tipoComprobante;
  private Long idUsuario;
  private Long idViajante;
  private Long numSerie;
  private Long numFactura;
  private Long nroPedido;
  private Long serieRemito;
  private Long nroRemito;
  private Long idProducto;
  private Long idSucursal;
  private Integer pagina;
  private String ordenarPor;
  private String sentido;
}
