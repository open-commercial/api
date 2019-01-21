package sic.modelo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "caja")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"id_Caja", "empresa"})
@ToString
@JsonIgnoreProperties({"empresa", "usuarioAbreCaja", "usuarioCierraCaja", "eliminada"})
public class Caja implements Serializable {

  @Id
  @GeneratedValue
  private long id_Caja;

  @Column(nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date fechaApertura;

  @Temporal(TemporalType.TIMESTAMP)
  private Date fechaCierre;

  @ManyToOne
  @JoinColumn(name = "id_Empresa", referencedColumnName = "id_Empresa")
  private Empresa empresa;

  @OneToOne
  @JoinColumn(name = "id_Usuario", referencedColumnName = "id_Usuario")
  private Usuario usuarioAbreCaja;

  @OneToOne
  @JoinColumn(name = "id_UsuarioCierra", referencedColumnName = "id_Usuario")
  private Usuario usuarioCierraCaja;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private EstadoCaja estado;

  @Column(precision = 25, scale = 15)
  private BigDecimal saldoApertura;

  @Column(precision = 25, scale = 15)
  private BigDecimal saldoSistema;

  @Column(precision = 25, scale = 15)
  private BigDecimal saldoReal;

  private boolean eliminada;

  @JsonGetter("idEmpresa")
  public Long getIdEmpresa() {
    return empresa.getId_Empresa();
  }

  @JsonGetter("nombreEmpresa")
  public String getNombreEmpresa() {
    return empresa.getNombre();
  }

  @JsonGetter("idUsuarioAbreCaja")
  public Long getIdUsuarioAbreCaja() {
    return usuarioAbreCaja.getId_Usuario();
  }

  @JsonGetter("nombreUsuarioAbreAcaja")
  public String getNombreUsuarioAbreCaja() {
    return usuarioAbreCaja.getNombre();
  }

  @JsonGetter("idUsuarioCierraCaja")
  public Long getIdUsuarioCierraCaja() {
    return (usuarioCierraCaja != null) ? usuarioCierraCaja.getId_Usuario() : null;
  }

  @JsonGetter("nombreUsuarioCierraAcaja")
  public String getNombreUsuarioCierraCaja() {
    return (usuarioCierraCaja != null) ? usuarioCierraCaja.getNombre() : null;
  }

}
