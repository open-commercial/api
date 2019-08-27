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
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

@Entity
@Table(name = "proveedor")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"razonSocial", "sucursal"})
@ToString
@JsonIgnoreProperties({"sucursal", "eliminado"})
public class Proveedor implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id_Proveedor;

  @Column(nullable = false)
  private String nroProveedor;

  @Column(nullable = false)
  @NotEmpty(message = "{mensaje_proveedor_razonSocial_vacia}")
  private String razonSocial;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  @NotNull(message = "{mensaje_proveedor_condicionIVA_vacia}")
  private CategoriaIVA categoriaIVA;

  private Long idFiscal;

  @Column(nullable = false)
  private String telPrimario;

  @Column(nullable = false)
  private String telSecundario;

  @Column(nullable = false)
  private String contacto;

  @Column(nullable = false)
  @Email(message = "{mensaje_proveedor_email_invalido}")
  private String email;

  @Column(nullable = false)
  private String web;

  @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
  @JoinColumn(name = "idUbicacion", referencedColumnName = "idUbicacion")
  @QueryInit("localidad.provincia")
  private Ubicacion ubicacion;

  private boolean eliminado;

}
