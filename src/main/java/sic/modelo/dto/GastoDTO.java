package sic.modelo.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(exclude = {"idGasto", "nroGasto", "idEmpresa", "nombreEmpresa", "fecha", "idUsuario", "nombreUsuario", "idFormaDePago", "nombreFormaDePago"})
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GastoDTO {

  private long idGasto;
  private long nroGasto;
  private LocalDateTime fecha;
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
