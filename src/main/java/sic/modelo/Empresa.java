package sic.modelo;

import java.io.Serializable;
import java.time.LocalDateTime;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;
import com.querydsl.core.annotations.QueryInit;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import sic.controller.Views;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

@Entity
@Table(name = "empresa")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"nombre"})
@ToString
@JsonView(Views.Comprador.class)
@JsonIgnoreProperties("eliminada")
public class Empresa implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_Empresa")
  private long idEmpresa;

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
  
  private LocalDateTime fechaInicioActividad;

  @Column(nullable = false)
  @Email(message = "{mensaje_correo_formato_incorrecto}")
  private String email;

  @Column(nullable = false)
  private String telefono;

  @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
  @JoinColumn(name = "idUbicacion", referencedColumnName = "idUbicacion")
  @QueryInit("localidad.provincia")
  private Ubicacion ubicacion;

  private String logo;

  private boolean eliminada;
}
