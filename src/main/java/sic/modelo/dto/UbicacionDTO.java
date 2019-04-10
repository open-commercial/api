package sic.modelo.dto;

import lombok.*;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Data
@EqualsAndHashCode(exclude = {"idUbicacion", "idProvincia", "nombreLocalidad", "codigoPostal", "idProvincia", "nombreProvincia"})
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Embeddable
public class UbicacionDTO implements Serializable {

  private long idUbicacion;
  private String descripcion;
  private Double latitud;
  private Double longitud;
  private String calle;
  private Integer numero;
  private String piso;
  private String departamento;
  private Long idLocalidad;
  private String nombreLocalidad;
  private String codigoPostal;
  private Long idProvincia;
  private String nombreProvincia;
}
