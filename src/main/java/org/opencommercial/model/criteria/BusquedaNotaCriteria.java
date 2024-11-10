package org.opencommercial.model.criteria;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.opencommercial.model.Movimiento;
import org.opencommercial.model.TipoDeComprobante;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BusquedaNotaCriteria {

  private Long idSucursal;
  private int cantidadDeRegistros;
  private boolean buscaPorNumeroNota;
  private LocalDateTime fechaDesde;
  private LocalDateTime fechaHasta;
  private Long numSerie;
  private Long numNota;
  private boolean buscaPorTipoComprobante;
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
