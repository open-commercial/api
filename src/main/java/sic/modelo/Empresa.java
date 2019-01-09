package sic.modelo;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonGetter;
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
public class Empresa implements Serializable {

  @Id @GeneratedValue private long id_Empresa;

  @Column(nullable = false)
  private String nombre;

  @Column(nullable = false)
  private String lema;

  @Column(nullable = false)
  private String direccion;

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

  @ManyToOne
  @JoinColumn(name = "id_Localidad", referencedColumnName = "id_Localidad")
  @QueryInit("provincia.pais")
  private Localidad localidad;

  private String logo;

  private boolean eliminada;

  @JsonGetter("idLocalidad")
  public Long getIdLocalidad() {
    return localidad.getId_Localidad();
  }

  @JsonGetter("nombreLocalidad")
  public String getNombreLocalidad() {
    return localidad.getNombre();
  }
}
