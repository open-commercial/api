package sic.modelo;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.querydsl.core.annotations.QueryInit;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "empresa")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"nombre"})
@ToString
@JsonIgnoreProperties("localidad")
public class Empresa implements Serializable {

  @Id @GeneratedValue private long id_Empresa;

  @Column(nullable = false)
  private String nombre;

  @Column(nullable = false)
  private String lema;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private CategoriaIVA categoriaIVA;

  private Long idFiscal;

  private Long ingresosBrutos;

  @Temporal(TemporalType.TIMESTAMP)
  private Date fechaInicioActividad;

  @Column(nullable = false)
  private String email;

  @Column(nullable = false)
  private String telefono;

  @OneToOne
  @JoinColumn(name = "idUbicacion", referencedColumnName = "idUbicacion")
  @QueryInit("localidad.provincia")
  @NotNull
  private Ubicacion ubicacion;

  private String logo;

  private boolean eliminada;

  @JsonGetter("idLocalidad")
  public Long getIdLocalidad() {
    return (ubicacion.getLocalidad() != null) ? ubicacion.getLocalidad().getId_Localidad() :  null;
  }

  @JsonGetter("nombreLocalidad")
  public String getNombreLocalidad() {
    return (ubicacion.getLocalidad() != null) ? ubicacion.getLocalidad().getNombre() : null;
  }
}
