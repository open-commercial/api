package org.opencommercial.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Entity
@Table(name = "productofavorito")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"producto", "cliente"})
public class ProductoFavorito implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long idProductoFavorito;

  @ManyToOne
  @JoinColumn(name = "id_Cliente")
  @NotNull(message = "{mensaje_cliente_vacio}")
  private Cliente cliente;

  @ManyToOne
  @JoinColumn(name = "idProducto")
  @NotNull(message = "{mensaje_producto_vacio}")
  private Producto producto;
}
