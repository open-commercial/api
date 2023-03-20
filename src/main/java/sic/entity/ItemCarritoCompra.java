package sic.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import sic.controller.Views;

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
  @JoinColumn(name = "idProducto", referencedColumnName = "idProducto")
  private Producto producto;

  @Transient
  private BigDecimal importe;

  @ManyToOne
  @JoinColumn(name = "id_Usuario", referencedColumnName = "id_Usuario")
  private Usuario usuario;
}
