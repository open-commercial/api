package sic.modelo.criteria;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sic.modelo.Movimiento;
import sic.modelo.TipoDeComprobante;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BusquedaNotaCriteria {

  private LocalDateTime fechaDesde;
  private LocalDateTime fechaHasta;
  private Long idEmpresa;
  private Long numSerie;
  private Long numNota;
  private TipoDeComprobante tipoComprobante;
  private Movimiento movimiento;
  private Long idUsuario;
  private Long idProveedor;
  private Long idCliente;
  private Long idViajante;
  private Integer pagina;
  private String ordenarPor;
  private String sentido;

}
