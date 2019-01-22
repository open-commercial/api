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
  private String direccion;

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

  @ManyToOne
  @JoinColumn(name = "id_Localidad", referencedColumnName = "id_Localidad")
  @QueryInit("provincia.pais")
  private Localidad localidad;

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
    return (localidad != null) ? localidad.getId_Localidad() : null;
  }

  @JsonGetter("nombreLocalidad")
  public String getNombreLocalidad() {
    return (localidad != null) ? localidad.getNombre() : null;
  }

  @JsonGetter("idProvincia")
  public Long getIdProvincia() {
    return (localidad != null) ? localidad.getProvincia().getId_Provincia() : null;
  }

  @JsonGetter("nombreProvincia")
  public String getNombreProvincia() {
    return (localidad != null) ? localidad.getProvincia().getNombre() : null;
  }

  @JsonGetter("idPais")
  public Long getIdPais() {
    return (localidad != null) ? localidad.getProvincia().getPais().getId_Pais() : null;
  }

  @JsonGetter("nombrePais")
  public String getNombrePais() {
    return (localidad != null) ? localidad.getProvincia().getPais().getNombre() : null;
  }
}
