package sic.modelo;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.querydsl.core.annotations.QueryInit;
import java.io.Serializable;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "proveedor")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"razonSocial", "empresa"})
@ToString
@JsonIgnoreProperties({"empresa", "eliminado", "localidad"})
public class Proveedor implements Serializable {

  @Id @GeneratedValue private long id_Proveedor;

  @Column(nullable = false)
  private String codigo;

  @Column(nullable = false)
  private String razonSocial;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private CategoriaIVA categoriaIVA;

  private Long idFiscal;

  @Column(nullable = false)
  private String telPrimario;

  @Column(nullable = false)
  private String telSecundario;

  @Column(nullable = false)
  private String contacto;

  @Column(nullable = false)
  private String email;

  @Column(nullable = false)
  private String web;

  @OneToOne
  @JoinColumn(name = "idUbicacion", referencedColumnName = "idUbicacion")
  @QueryInit("localidad.provincia")
  @NotNull
  private Ubicacion ubicacion;

  @ManyToOne
  @JoinColumn(name = "id_Empresa", referencedColumnName = "id_Empresa")
  private Empresa empresa;

  private boolean eliminado;

  @JsonGetter("nombreEmpresa")
  public String getNombreEmpresa() {
    return empresa.getNombre();
  }

  @JsonGetter("idEmpresa")
  public long getIdEmpresa() {
    return empresa.getId_Empresa();
  }

  @JsonGetter("idLocalidad")
  public Long getIdLocalidad() {
    return (ubicacion.getLocalidad() != null) ? ubicacion.getLocalidad().getId_Localidad() : null;
  }

  @JsonGetter("nombreLocalidad")
  public String getNombreLocalidad() {
    return (ubicacion.getLocalidad() != null) ? ubicacion.getLocalidad().getNombre() : null;
  }

  @JsonGetter("idProvincia")
  public Long getIdProvincia() {
    return (ubicacion.getLocalidad() != null) ? ubicacion.getLocalidad().getProvincia().getId_Provincia() : null;
  }

  @JsonGetter("nombreProvincia")
  public String getNombreProvincia() {
    return (ubicacion.getLocalidad() != null) ? ubicacion.getLocalidad().getProvincia().getNombre() : null;
  }
}
