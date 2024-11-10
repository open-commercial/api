package org.opencommercial.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import lombok.*;
import org.opencommercial.config.Views;

import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "itemcarritocompra")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"producto", "usuario"})
@ToString
@JsonView(Views.Comprador.class)
@JsonIgnoreProperties("usuario")
public class ItemCarritoCompra implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long idItemCarritoCompra;

  @Column(precision = 25, scale = 15)
  private BigDecimal cantidad;

  @ManyToOne
  @JoinColumn(name = "idProducto")
  private Producto producto;

  @Transient
  private BigDecimal importe;

  @ManyToOne
  @JoinColumn(name = "id_Usuario")
  private Usuario usuario;
}
