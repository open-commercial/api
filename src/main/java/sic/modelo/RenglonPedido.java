package sic.modelo;

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
@Table(name = "renglonpedido")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "producto")
@ToString
public class RenglonPedido implements Serializable {

    @Id
    @GeneratedValue
    private long id_RenglonPedido;

    @ManyToOne
    @JoinColumn(name = "id_Producto", referencedColumnName = "id_Producto")
    private Producto producto;
    
    @Column(precision = 18, scale = 15)
    private BigDecimal cantidad;
    
    @Column(precision = 18, scale = 15)
    private BigDecimal descuento_porcentaje;
    
    @Column(precision = 18, scale = 15)
    private BigDecimal descuento_neto;
    
    @Column(precision = 18, scale = 15)
    private BigDecimal subTotal;

}