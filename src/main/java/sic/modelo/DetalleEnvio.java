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

  private Long latitud;

  private Long longitud;

  private String calle; //req

  private Integer numero; // req

  private Integer piso;

  private String departamento;

  private Integer codigoPostal;
}
