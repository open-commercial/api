package sic.modelo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
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

@Entity
@Table(name = "itemcarritocompra")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"producto", "usuario"})
public class ItemCarritoCompra implements Serializable {

    @Id
    @GeneratedValue
    private Long idItemCarritoCompra;

    private double cantidad;

    @ManyToOne
    @JoinColumn(name = "id_Producto", referencedColumnName = "id_Producto")
    private Producto producto;

    private double importe;

    @ManyToOne
    @JoinColumn(name = "id_Usuario", referencedColumnName = "id_Usuario")
    @JsonIgnore
    private Usuario usuario;

}
