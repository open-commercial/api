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
public class BusquedaFacturaVentaCriteria {

  private boolean buscaPorFecha;
  private Date fechaDesde;
  private Date fechaHasta;
  private boolean buscaCliente;
  private Long idCliente;
  private boolean buscaPorTipoComprobante;
  private TipoDeComprobante tipoComprobante;
  private boolean buscaUsuario;
  private Long idUsuario;
  private boolean buscaViajante;
  private Long idViajante;
  private boolean buscaPorNumeroFactura;
  private long numSerie;
  private long numFactura;
  private boolean buscarPorPedido;
  private long nroPedido;
  private boolean buscaPorProducto;
  private Long idProducto;
  private Long idSucursal;
  private Integer pagina;
  private String ordenarPor;
  private String sentido;
}
