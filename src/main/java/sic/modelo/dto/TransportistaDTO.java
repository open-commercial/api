package sic.modelo.dto;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(exclude = {"id_Transportista"})
@Builder
public class TransportistaDTO {

  private long id_Transportista;
  private String nombre;
  private String direccion;
  private LocalidadDTO localidad;
  private String web;
  private String telefono;
  private EmpresaDTO empresa;
  private boolean eliminado;
}
