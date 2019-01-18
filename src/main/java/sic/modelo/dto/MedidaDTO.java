package sic.modelo.dto;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(exclude = {"id_Medida", "idEmpresa", "nombreEmpresa"})
@Builder
public class MedidaDTO {

  private long id_Medida;
  private String nombre;
  private Long idEmpresa;
  private String nombreEmpresa;
  private boolean eliminada;
}
