package sic.modelo.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UbicacionDTO {

  private long idUbicacion;

  private String descripcion;

  private Long latitud;

  private Long longitud;

  private String calle;

  private Integer numero;

  private Integer piso;

  private String departamento;

  private Integer codigoPostal;

  private boolean eliminada;

  private Long idLocalidad;

  private String nombreLocalidad;

  private Long idProvincia;

  private String nombreProvincia;
}
