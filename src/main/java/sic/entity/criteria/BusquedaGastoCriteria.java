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
public class BusquedaGastoCriteria {

  private Long idSucursal;
  private boolean buscaPorUsuario;
  private LocalDateTime fechaDesde;
  private LocalDateTime fechaHasta;
  private Long idUsuario;
  private Long idFormaDePago;
  private Long nroGasto;
  private String concepto;
  private Integer pagina;
  private String ordenarPor;
  private String sentido;
}
