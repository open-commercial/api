package sic.modelo.criteria;

import java.util.Date;
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

  private boolean buscaPorFecha;
  private Date fechaDesde;
  private Date fechaHasta;
  private boolean buscaPorProveedor;
  private Long idProveedor;
  private boolean buscaPorNumeroFactura;
  private long numSerie;
  private long numFactura;
  private boolean buscaPorTipoComprobante;
  private TipoDeComprobante tipoComprobante;
  private boolean buscaPorProducto;
  private Long idProducto;
  private Long idSucursal;
  private Integer pagina;
  private String ordenarPor;
  private String sentido;
}
