package sic.modelo.dto;

import lombok.*;

@Data
@EqualsAndHashCode(exclude = {"id_Medida", "idEmpresa", "nombreEmpresa"})
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MedidaDTO {

  private long id_Medida;
  private String nombre;
  private Long idEmpresa;
  private String nombreEmpresa;
  private boolean eliminada;
}
