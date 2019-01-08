package sic.modelo;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.querydsl.core.annotations.QueryInit;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
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
public class Transportista implements Serializable {

  @Id @GeneratedValue private long id_Transportista;

  @Column(nullable = false)
  private String nombre;

  @Column(nullable = false)
  private String direccion;

  @ManyToOne
  @JoinColumn(name = "id_Localidad", referencedColumnName = "id_Localidad")
  @QueryInit("provincia.pais")
  private Localidad localidad;

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
    return localidad.getId_Localidad();
  }

  @JsonGetter("nombreLocalidad")
  public String getNombreLocalidad() {
    return localidad.getNombre();
  }

  @JsonGetter("nombreProvincia")
  public String getNombreProvincia() {
    return localidad.getProvincia().getNombre();
  }

  @JsonGetter("nombrePais")
  public String getNombrePais() {
    return localidad.getProvincia().getPais().getNombre();
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
