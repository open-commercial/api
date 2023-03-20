package sic.entity.criteria;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BusquedaRemitoCriteria {

  private LocalDateTime fechaDesde;
  private LocalDateTime fechaHasta;
  private Long serieRemito;
  private Long nroRemito;
  private Long idCliente;
  private Long idSucursal;
  private Long idUsuario;
  private Long idTransportista;
  private Long serieFacturaVenta;
  private Long nroFacturaVenta;
  private Integer pagina;
  private String ordenarPor;
  private String sentido;
}
