package org.opencommercial.integration.model;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@EqualsAndHashCode(
    callSuper = true,
    exclude = {"renglonesNotaDebito"})
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotaDebitoTest extends NotaTest {

  private List<RenglonNotaDebitoTest> renglonesNotaDebito;
  private BigDecimal montoNoGravado;
  private Long idRecibo;
  private boolean pagada;
}
