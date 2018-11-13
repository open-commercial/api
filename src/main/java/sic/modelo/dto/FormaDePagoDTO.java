package sic.modelo.dto;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(exclude = {"id_FormaDePago"})
@Builder
public class FormaDePagoDTO {

  private long id_FormaDePago;
  private String nombre;
  private boolean afectaCaja;
  private boolean predeterminado;
  private EmpresaDTO empresa;
  private boolean eliminada;
}
