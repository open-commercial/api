package sic.modelo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CondicionIVADTO {

  private long id_CondicionIVA;
  private String nombre;
  private boolean discriminaIVA;
  private boolean eliminada;

}
