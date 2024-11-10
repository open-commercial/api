package org.opencommercial.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;
import com.querydsl.core.annotations.QueryInit;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.opencommercial.config.Views;

import java.io.Serializable;

@Entity
@Table(name = "proveedor")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "razonSocial")
@ToString
@JsonIgnoreProperties("eliminado")
@JsonView(Views.Comprador.class)
public class Proveedor implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_Proveedor")
  private long idProveedor;

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

  private String telPrimario;

  private String telSecundario;

  private String contacto;

  @Email(message = "{mensaje_proveedor_email_invalido}")
  private String email;

  private String web;

  @ManyToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "idUbicacion")
  @QueryInit("localidad.provincia")
  private Ubicacion ubicacion;

  private boolean eliminado;
}
