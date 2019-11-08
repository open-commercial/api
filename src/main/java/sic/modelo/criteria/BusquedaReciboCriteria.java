package sic.modelo.criteria;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sic.modelo.Movimiento;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BusquedaReciboCriteria {

  private LocalDateTime fechaDesde;
  private LocalDateTime fechaHasta;
  private Long idSucursal;
  private Long numSerie;
  private Long numRecibo;
  private String concepto;
  private Long idCliente;
  private Long idProveedor;
  private Long idUsuario;
  private Long idViajante;
  private Movimiento movimiento;
  private Integer pagina;
  private String ordenarPor;
  private String sentido;
}
