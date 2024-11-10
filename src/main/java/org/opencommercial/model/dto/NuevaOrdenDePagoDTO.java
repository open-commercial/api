package org.opencommercial.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.opencommercial.model.Movimiento;
import org.opencommercial.model.TipoDeEnvio;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NuevaOrdenDePagoDTO {

  @NotNull(message = "{mensaje_preference_sin_movimiento}")
  private Movimiento movimiento;

  private long idSucursal;

  private TipoDeEnvio tipoDeEnvio;

  private BigDecimal monto;
}
