package sic.modelo.dto;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(exclude = {"id_Rubro"})
@Builder
public class RubroDTO {

  private long id_Rubro;
  private String nombre;
  private EmpresaDTO empresa;
  private boolean eliminado;
}
