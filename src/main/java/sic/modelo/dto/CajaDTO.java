package sic.modelo.dto;

import lombok.*;
import sic.modelo.EstadoCaja;

import java.math.BigDecimal;
import java.util.Date;

@Data
@EqualsAndHashCode(exclude = {"id_Caja", "idEmpresa", "nombreEmpresa", "idUsuarioAbreCaja", "nombreUsuarioAbreCaja",
  "idUsuarioCierraCaja", "nombreUsuarioCierraCaja", "fechaApertura", "saldoApertura", "saldoSistema", "saldoReal"})
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CajaDTO {

  private long id_Caja;
  private Date fechaApertura;
  private Date fechaCierre;
  private Long idEmpresa;
  private String nombreEmpresa;
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
