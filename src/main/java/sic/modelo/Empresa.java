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
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

@Entity
@Table(name = "empresa")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"nombre"})
@ToString
@JsonIgnoreProperties({"ubicacion", "eliminada"})
public class Empresa implements Serializable {

  @Id @GeneratedValue private long id_Empresa;

  @NotNull(message = "{mensaje_empresa_nombre_vacio}")
  @NotEmpty(message = "{mensaje_empresa_nombre_vacio}")
  @Column(nullable = false)
  private String nombre;

  @Column(nullable = false)
  private String lema;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  @NotNull(message = "{mensaje_empresa_condicion_iva_vacia}")
  private CategoriaIVA categoriaIVA;

  private Long idFiscal;

  private Long ingresosBrutos;

  @Temporal(TemporalType.TIMESTAMP)
  private Date fechaInicioActividad;

  @Column(nullable = false)
  @Email(message = "{mensaje_correo_formato_incorrecto}")
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
