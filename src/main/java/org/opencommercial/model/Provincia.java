package org.opencommercial.model;

import java.io.Serializable;
import jakarta.persistence.*;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.opencommercial.config.Views;

@Entity
@Table(name = "provincia")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"nombre"})
@ToString
@JsonView(Views.Comprador.class)
public class Provincia implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long idProvincia;

  @Column(nullable = false)
  private String nombre;
}
