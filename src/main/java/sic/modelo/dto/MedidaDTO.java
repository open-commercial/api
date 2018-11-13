package sic.modelo.dto;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(exclude = {"id_Medida"})
@Builder
public class MedidaDTO {

  private long id_Medida;
  private String nombre;
  private EmpresaDTO empresa;
  private boolean eliminada;
}
