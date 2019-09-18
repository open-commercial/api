package sic.modelo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BusquedaReciboCriteria {

  private Date fechaDesde;
  private Date fechaHasta;
  private Long idEmpresa;
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
