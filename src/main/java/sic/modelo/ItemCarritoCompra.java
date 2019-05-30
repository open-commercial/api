package sic.modelo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.math.BigDecimal;
import javax.persistence.*;

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
public class ItemCarritoCompra implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long idItemCarritoCompra;

  @JsonView(Views.Public.class)
  @Column(precision = 25, scale = 15)
  private BigDecimal cantidad;

  @JsonView(Views.Public.class)
  @ManyToOne
  @JoinColumn(name = "idProducto", referencedColumnName = "idProducto")
  private Producto producto;

  @JsonView(Views.Public.class)
  @Transient
  private BigDecimal importe;

  @JsonView(Views.Public.class)
  @Transient
  private BigDecimal importeBonificado;

  @ManyToOne
  @JoinColumn(name = "id_Usuario", referencedColumnName = "id_Usuario")
  @JsonIgnore
  private Usuario usuario;
}
