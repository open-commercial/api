package sic.modelo.criteria;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BusquedaGastoCriteria {

  private Date fechaDesde;
  private Date fechaHasta;
  private Long idSucursal;
  private boolean buscaPorUsuario;
  private Long idUsuario;
  private Long idFormaDePago;
  private Long nroGasto;
  private String concepto;
  private Integer pagina;
  private String ordenarPor;
  private String sentido;
}
