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

  private boolean buscaPorFecha;
  private Date fechaDesde;
  private Date fechaHasta;
  private Long idEmpresa;
  private boolean buscaPorNumeroRecibo;
  private Long numSerie;
  private Long numRecibo;
  private boolean buscaPorConcepto;
  private String concepto;
  private boolean buscaPorCliente;
  private Long idCliente;
  private boolean buscaPorProveedor;
  private Long idProveedor;
  private boolean buscaPorUsuario;
  private Long idUsuario;
  private boolean buscaPorViajante;
  private Long idViajante;
  private Movimiento movimiento;
  private Integer pagina;
  private String ordenarPor;
  private String sentido;
}
