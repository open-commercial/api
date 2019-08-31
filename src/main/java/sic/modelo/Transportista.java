package sic.modelo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;
import com.querydsl.core.annotations.QueryInit;
import java.io.Serializable;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import sic.controller.Views;

import javax.validation.constraints.NotEmpty;

@Entity
@Table(name = "transportista")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "nombre")
@ToString
@JsonIgnoreProperties({"localidad", "eliminado"})
@JsonView(Views.Comprador.class)
public class Transportista implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id_Transportista;

  @Column(nullable = false)
  @NotNull(message = "{mensaje_transportista_nombre_vacio}")
  @NotEmpty(message = "{mensaje_transportista_nombre_vacio}")
  private String nombre;

  @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
  @JoinColumn(name = "idUbicacion", referencedColumnName = "idUbicacion")
  @QueryInit("localidad.provincia")
  private Ubicacion ubicacion;

  @Column(nullable = false)
  private String web;

  @Column(nullable = false)
  private String telefono;

  private boolean eliminado;
}
