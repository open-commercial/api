package sic.modelo.dto;

import lombok.*;
import sic.modelo.CategoriaIVA;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(exclude = {"idEmpresa" , "ubicacion"})
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmpresaDTO {

  private long idEmpresa;
  private String nombre;
  private String lema;
  private CategoriaIVA categoriaIVA;
  private Long idFiscal;
  private long ingresosBrutos;
  private LocalDateTime fechaInicioActividad;
  private String email;
  private String telefono;
  private UbicacionDTO ubicacion;
  private String logo;
  private boolean eliminada;

}
