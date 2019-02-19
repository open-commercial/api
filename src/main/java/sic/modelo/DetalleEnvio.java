package sic.modelo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(exclude = "detalleUbicacion")
@Embeddable
public class DetalleEnvio {

  private String descripcion;

  private Double latitud;

  private Double longitud;

  @NotNull
  private String calle;

  @NotNull
  private Integer numero;

  private Integer piso;

  private String departamento;

  private String codigoPostal;

  @NotNull
  private String nombreLocalidad;

  @NotNull
  private String nombreProvincia;

  private String detalleUbicacion;
}
