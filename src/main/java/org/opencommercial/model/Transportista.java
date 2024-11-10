package org.opencommercial.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;
import com.querydsl.core.annotations.QueryInit;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.opencommercial.config.Views;

import java.io.Serializable;

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
  @Column(name = "id_Transportista")
  private long idTransportista;

  @Column(nullable = false)
  @NotNull(message = "{mensaje_transportista_nombre_vacio}")
  @NotEmpty(message = "{mensaje_transportista_nombre_vacio}")
  private String nombre;

  @ManyToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "idUbicacion")
  @QueryInit("localidad.provincia")
  private Ubicacion ubicacion;

  @Column(nullable = false)
  private String web;

  @Column(nullable = false)
  private String telefono;

  private boolean eliminado;
}
