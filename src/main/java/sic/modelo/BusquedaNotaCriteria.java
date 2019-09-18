package sic.modelo;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BusquedaNotaCriteria {

  private Date fechaDesde;
  private Date fechaHasta;
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
