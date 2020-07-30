package sic.modelo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NuevoRemitoDTO {

  private long idFacturaVenta;
  private boolean dividir;
  private boolean contraEntrega;
}
