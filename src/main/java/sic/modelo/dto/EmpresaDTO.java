package sic.modelo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sic.modelo.CategoriaIVA;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmpresaDTO {

  private long id_Empresa;
  private String nombre;
  private String lema;
  private String direccion;
  private CategoriaIVA categoriaIVA;
  private long cuip;
  private long ingresosBrutos;
  private Date fechaInicioActividad;
  private String email;
  private String telefono;
  private LocalidadDTO localidad;
  private String logo;
  private boolean eliminada;

}
