package sic.modelo.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

@Data
@EqualsAndHashCode(exclude = {"id_Gasto", "nroGasto", "idEmpresa", "nombreEmpresa", "fecha", "idUsuario", "nombreUsuario", "idFormaDePago", "nombreFormaDePago"})
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GastoDTO {

  private long id_Gasto;
  private long nroGasto;
  private Date fecha;
  private String concepto;
  private Long idEmpresa;
  private String nombreEmpresa;
  private Long idUsuario;
  private String nombreUsuario;
  private Long idFormaDePago;
  private String nombreFormaDePago;
  private BigDecimal monto;
  private boolean eliminado;
}
