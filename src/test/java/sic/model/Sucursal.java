package sic.model;

import lombok.*;
import sic.modelo.CategoriaIVA;
import sic.modelo.ConfiguracionSucursal;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"idSucursal", "ubicacion", "detalleUbicacion", "configuracionSucursal"})
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
  private ConfiguracionSucursal configuracionSucursal;
  private String detalleUbicacion;
  private String logo;
  private boolean eliminada;
}
