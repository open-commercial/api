package sic.model;

import lombok.*;
import sic.modelo.CategoriaIVA;

import java.time.LocalDateTime;
import java.time.Month;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"idSucursal", "ubicacion", "detalleUbicacion"})
public class Sucursal {

  private long idSucursal;
  private String nombre;
  private String lema;
  private CategoriaIVA categoriaIVA;
  private Long idFiscal;
  private long ingresosBrutos;
  private LocalDateTime fechaInicioActividad;
  private String email;
  private String telefono;
  private Ubicacion ubicacion;
  private String detalleUbicacion;
  private String logo;
  private boolean eliminada;
}
