package sic.modelo.criteria;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sic.modelo.TipoDeComprobante;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BusquedaFacturaCompraCriteria {

  private LocalDateTime fechaDesde;
  private LocalDateTime fechaHasta;
  private Long idProveedor;
  private Long numSerie;
  private Long numFactura;
  private TipoDeComprobante tipoComprobante;
  private Long idProducto;
  private Long idEmpresa;
  private Integer pagina;
  private String ordenarPor;
  private String sentido;
}
