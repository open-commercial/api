package sic.modelo.dto;

import lombok.*;

@Data
@EqualsAndHashCode(exclude = {"id_FormaDePago", "idEmpresa", "nombreEmpresa"})
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FormaDePagoDTO {

  private long id_FormaDePago;
  private String nombre;
  private boolean afectaCaja;
  private boolean predeterminado;
  private Long idEmpresa;
  private String nombreEmpresa;
  private boolean eliminada;
}
