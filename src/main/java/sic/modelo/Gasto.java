package sic.modelo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.querydsl.core.annotations.QueryInit;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "gasto")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"nroGasto", "empresa"})
@ToString
@JsonIgnoreProperties({"empresa", "eliminado", "usuario"})
public class Gasto implements Serializable {

  @Id @GeneratedValue private long id_Gasto;

  private long nroGasto;

  @Column(nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date fecha;

  @Column(nullable = false)
  private String concepto;

  @ManyToOne
  @JoinColumn(name = "id_Empresa", referencedColumnName = "id_Empresa")
  @QueryInit("ubicacion.localidad.provincia")
  private Empresa empresa;

  @OneToOne
  @JoinColumn(name = "id_Usuario", referencedColumnName = "id_Usuario")
  private Usuario usuario;

  @OneToOne
  @JoinColumn(name = "id_FormaDePago", referencedColumnName = "id_FormaDePago")
  private FormaDePago formaDePago;

  @Column(precision = 25, scale = 15)
  private BigDecimal monto;

  private boolean eliminado;

  @JsonGetter("idEmpresa")
  public Long getIdEmpresa() {
    return empresa.getId_Empresa();
  }

  @JsonGetter("nombreEmpresa")
  public String getNombreEmpresa() {
    return empresa.getNombre();
  }

  @JsonGetter("idFormaDePago")
  public Long getIdFormaDePago() {
    return formaDePago.getId_FormaDePago();
  }

  @JsonGetter("nombreFormaDePago")
  public String getNombreFormaDePago() {
    return formaDePago.getNombre();
  }

  @JsonGetter("idUsuario")
  public Long getIdCredencial() {
    return usuario.getId_Usuario();
  }

  @JsonGetter("nombreUsuario")
  public String getNombreUsuario() {
    return usuario.getNombre() + " " + usuario.getApellido() + " (" + usuario.getUsername() + ")";
  }
}
