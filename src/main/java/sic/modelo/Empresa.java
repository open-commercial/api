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
  private Ubicacion ubicacion;

  private String logo;

  private boolean eliminada;

  @JsonGetter("idUbicacion")
  public Long getidUbicacion() {
    if (ubicacion != null) {
      return ubicacion.getIdUbicacion();
    } else {
      return null;
    }
  }

  @JsonGetter("detalleUbicacion")
  public String getDetalleUbicacion() {
    if (ubicacion != null) {
      return ubicacion.toString();
    } else {
      return null;
    }
  }
}
