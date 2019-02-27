package sic.modelo;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.querydsl.core.annotations.QueryInit;
import java.io.Serializable;
import javax.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "transportista")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"nombre", "empresa"})
@ToString
@JsonIgnoreProperties({"localidad", "empresa", "eliminado"})
public class Transportista implements Serializable {

  @Id @GeneratedValue private long id_Transportista;

  @Column(nullable = false)
  private String nombre;

  @OneToOne
  @JoinColumn(name = "idUbicacion", referencedColumnName = "idUbicacion")
  @QueryInit("localidad.provincia")
  private Ubicacion ubicacion;

  @Column(nullable = false)
  private String web;

  @Column(nullable = false)
  private String telefono;

  @ManyToOne
  @JoinColumn(name = "id_Empresa", referencedColumnName = "id_Empresa")
  private Empresa empresa;

  private boolean eliminado;

  @JsonGetter("idLocalidad")
  public Long getIdLocalidad() {
    return (ubicacion != null && ubicacion.getLocalidad() != null)
        ? ubicacion.getLocalidad().getId_Localidad()
        : null;
  }

  @JsonGetter("nombreLocalidad")
  public String getNombreLocalidad() {
    return (ubicacion != null && ubicacion.getLocalidad() != null)
        ? ubicacion.getLocalidad().getNombre()
        : null;
  }

  @JsonGetter("nombreProvincia")
  public String getNombreProvincia() {
    return (ubicacion != null && ubicacion.getLocalidad() != null)
        ? ubicacion.getLocalidad().getProvincia().getNombre()
        : null;
  }

  @JsonGetter("idEmpresa")
  public Long getIdEmpresa() {
    return empresa.getId_Empresa();
  }

  @JsonGetter("nombreEmpresa")
  public String getNombreEmpresa() {
    return empresa.getNombre();
  }
}
