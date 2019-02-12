package sic.modelo.dto;

import lombok.*;
import sic.modelo.CategoriaIVA;

import java.util.Date;

@Data
@EqualsAndHashCode(exclude = {"id_Empresa", "ubicacion", "nombreLocalidad"})
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmpresaDTO {

  private long id_Empresa;
  private String nombre;
  private String lema;
  private String direccion;
  private CategoriaIVA categoriaIVA;
  private Long idFiscal;
  private long ingresosBrutos;
  private Date fechaInicioActividad;
  private String email;
  private String telefono;
  private UbicacionDTO ubicacion;
  private String nombreLocalidad;
  private String logo;
  private boolean eliminada;

}
