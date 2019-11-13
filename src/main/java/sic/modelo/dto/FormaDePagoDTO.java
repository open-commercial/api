package sic.modelo.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"idFormaDePago", "idEmpresa", "nombreEmpresa"})
@Builder
public class FormaDePagoDTO {

  private long idFormaDePago;
  private String nombre;
  private boolean afectaCaja;
  private boolean predeterminado;
  private Long idEmpresa;
  private String nombreEmpresa;
  private boolean eliminada;
}
