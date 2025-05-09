package org.opencommercial.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;
import com.querydsl.core.annotations.QueryInit;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.opencommercial.config.Views;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "sucursal")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"nombre"})
@ToString(exclude = "logo")
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

  @ManyToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "idUbicacion")
  @QueryInit("localidad.provincia")
  @NotNull(message = "{mensaje_sucursal_sin_ubicacion}")
  private Ubicacion ubicacion;

  @ManyToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "idConfiguracionSucursal")
  private ConfiguracionSucursal configuracionSucursal;

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
