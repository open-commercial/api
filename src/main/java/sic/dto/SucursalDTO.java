package sic.dto;

import lombok.*;
import sic.domain.CategoriaIVA;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(exclude = {"idSucursal" , "ubicacion", "detalleUbicacion"})
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SucursalDTO {

  private long idSucursal;
  private String nombre;
  private String lema;
  private CategoriaIVA categoriaIVA;
  private Long idFiscal;
  private long ingresosBrutos;
  private LocalDateTime fechaInicioActividad;
  private String email;
  private String telefono;
  private UbicacionDTO ubicacion;
  private String detalleUbicacion;
  private String logo;
  private byte[] imagen;
  private boolean eliminada;

}
