package sic.modelo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
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

    @Id
    @GeneratedValue
    private Long idItemCarritoCompra;

    @Column(precision = 18, scale = 15)
    private BigDecimal cantidad;

    @ManyToOne
    @JoinColumn(name = "id_Producto", referencedColumnName = "id_Producto")
    private Producto producto;

    @Column(precision = 18, scale = 15)
    private BigDecimal importe;

    @ManyToOne
    @JoinColumn(name = "id_Usuario", referencedColumnName = "id_Usuario")
    @JsonIgnore
    private Usuario usuario;

}
