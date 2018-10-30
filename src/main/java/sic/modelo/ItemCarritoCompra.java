package sic.modelo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.math.BigDecimal;
import javax.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "itemcarritocompra")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"producto", "usuario"})
@ToString
public class ItemCarritoCompra implements Serializable {

  @Id @GeneratedValue private Long idItemCarritoCompra;

  @Column(precision = 25, scale = 15)
  private BigDecimal cantidad;

  @ManyToOne
  @JoinColumn(name = "idProducto", referencedColumnName = "idProducto")
  private Producto producto;

  @Transient private BigDecimal importe;

  @ManyToOne
  @JoinColumn(name = "id_Usuario", referencedColumnName = "id_Usuario")
  @JsonIgnore
  private Usuario usuario;
}
