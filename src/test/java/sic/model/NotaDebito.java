package sic.model;

import lombok.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Data
@EqualsAndHashCode(
    callSuper = true,
    exclude = {"renglonesNotaDebito"})
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotaDebito extends Nota {

  private List<RenglonNotaDebito> renglonesNotaDebito;
  private BigDecimal montoNoGravado;
  private Long idRecibo = 0L;
  private boolean pagada = true;
}
