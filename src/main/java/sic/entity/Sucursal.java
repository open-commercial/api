package sic.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import javax.persistence.*;
import javax.validation.constraints.*;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;
import com.querydsl.core.annotations.QueryInit;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import sic.controller.Views;
import sic.domain.CategoriaIVA;

@Entity
@Table(name = "sucursal")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"nombre"})
@ToString
@JsonView(Views.Comprador.class)
@JsonIgnoreProperties("eliminada")
public class Sucursal implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long idSucursal;

  @NotNull(message = "{mensaje_sucursal_nombre_vacio}")
  @NotEmpty(message = "{mensaje_sucursal_nombre_vacio}")
  @Column(nullable = false)
  private String nombre;

  private String lema;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  @NotNull(message = "{mensaje_sucursal_condicion_iva_vacia}")
  private CategoriaIVA categoriaIVA;

  private Long idFiscal;

  private Long ingresosBrutos;
  
  private LocalDateTime fechaInicioActividad;

  @Column(nullable = false)
  @Email(message = "{mensaje_correo_formato_incorrecto}")
  @NotBlank(message = "{mensaje_correo_vacio}")
  private String email;

  private String telefono;

  @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
  @JoinColumn(name = "idUbicacion", referencedColumnName = "idUbicacion")
  @QueryInit("localidad.provincia")
  @NotNull(message = "{mensaje_sucursal_sin_ubicacion}")
  private Ubicacion ubicacion;

  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "idConfiguracionSucursal", referencedColumnName = "idConfiguracionSucursal")
  private ConfiguracionSucursal configuracionSucursal;

  @Pattern(
      regexp = "^https:\\/\\/res.cloudinary.com\\/.*",
      message = "{mensaje_url_imagen_no_valida}")
  private String logo;

  private boolean eliminada;

  @JsonGetter("detalleUbicacion")
  public String getDetalleUbicacion() {
    String detalleUbicacion = "";
    if (ubicacion != null) {
      detalleUbicacion =
          (ubicacion.getCalle() != null ? ubicacion.getCalle() + " " : "")
              + (ubicacion.getNumero() != null ? ubicacion.getNumero() + " " : "")
              + (ubicacion.getPiso() != null ? ubicacion.getPiso() + " " : "")
              + (ubicacion.getDepartamento() != null ? ubicacion.getDepartamento() + " " : "")
              + (ubicacion.getNombreLocalidad() != null ? ubicacion.getNombreLocalidad() + " " : "")
              + (ubicacion.getNombreProvincia() != null ? ubicacion.getNombreProvincia() : "");
    }
    return detalleUbicacion;
  }
}
