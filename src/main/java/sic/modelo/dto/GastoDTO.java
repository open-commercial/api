package sic.modelo.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(exclude = {"id_Gasto", "nroGasto", "idSucursal", "nombreSucursal", "fecha", "idUsuario", "nombreUsuario", "idFormaDePago", "nombreFormaDePago"})
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GastoDTO {

  private long id_Gasto;
  private long nroGasto;
  private LocalDateTime fecha;
  private String concepto;
  private Long idSucursal;
  private String nombreSucursal;
  private Long idUsuario;
  private String nombreUsuario;
  private Long idFormaDePago;
  private String nombreFormaDePago;
  private BigDecimal monto;
  private boolean eliminado;
}
