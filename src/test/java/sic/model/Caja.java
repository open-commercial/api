package sic.model;

import lombok.*;
import sic.domain.EstadoCaja;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(
    exclude = {
      "idCaja",
      "idSucursal",
      "nombreSucursal",
      "idUsuarioAbreCaja",
      "nombreUsuarioAbreCaja",
      "idUsuarioCierraCaja",
      "nombreUsuarioCierraCaja",
      "fechaApertura",
      "saldoApertura",
      "saldoSistema",
      "saldoReal"
    })
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Caja {

  private long idCaja;
  private LocalDateTime fechaApertura;
  private LocalDateTime fechaCierre;
  private Long idSucursal;
  private String nombreSucursal;
  private Long idUsuarioAbreCaja;
  private String nombreUsuarioAbreCaja;
  private Long idUsuarioCierraCaja;
  private String nombreUsuarioCierraCaja;
  private EstadoCaja estado;
  private BigDecimal saldoApertura;
  private BigDecimal saldoSistema;
  private BigDecimal saldoReal;
  private boolean eliminada;
}
