package sic.modelo.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(exclude = {"idGasto", "idSucursal", "idFormaDePago"})
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NuevoGastoDTO {

  private long idGasto;
  private String concepto;
  private Long idSucursal;
  private Long idFormaDePago;
  private BigDecimal monto;
}
