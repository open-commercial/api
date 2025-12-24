package org.opencommercial.integration.model;

import lombok.*;
import org.opencommercial.model.CategoriaIVA;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(
        exclude = {
                "idSucursal",
                "ubicacion",
                "detalleUbicacion",
                "configuracionSucursal"
        })
public class SucursalTest {

  private long idSucursal;
  private String nombre;
  private String lema;
  private CategoriaIVA categoriaIVA;
  private Long idFiscal;
  private long ingresosBrutos;
  private LocalDateTime fechaInicioActividad;
  private String email;
  private String telefono;
  private UbicacionTest ubicacion;
  private ConfiguracionSucursalTest configuracionSucursal;
  private String detalleUbicacion;
  private String logo;
  private boolean eliminada;
}
