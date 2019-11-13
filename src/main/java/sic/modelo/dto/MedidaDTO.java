package sic.modelo.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"idMedida", "idEmpresa", "nombreEmpresa"})
@Builder
public class MedidaDTO {

  private long idMedida;
  private String nombre;
  private Long idEmpresa;
  private String nombreEmpresa;
  private boolean eliminada;
}
