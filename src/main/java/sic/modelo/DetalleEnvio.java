package sic.modelo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class DetalleEnvio {

  private String descripcion;

  private Double latitud;

  private Double longitud;

  private String calle;

  private Integer numero;

  private Integer piso;

  private String departamento;

  private Integer codigoPostal;

  private String nombreLocalidad;

  private String nombreProvincia;
}
