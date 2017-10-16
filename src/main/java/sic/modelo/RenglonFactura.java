package sic.modelo;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "renglonfactura")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"id_ProductoItem", "codigoItem"})
public class RenglonFactura implements Serializable {

    @Id
    @GeneratedValue
    private long id_RenglonFactura;

    private long id_ProductoItem;

    @Column(nullable = false)
    private String codigoItem;

    @Column(nullable = false)
    private String descripcionItem;

    @Column(nullable = false)
    private String medidaItem;

    private double cantidad;
    private double precioUnitario;
    private double descuento_porcentaje;
    private double descuento_neto;
    private double iva_porcentaje;
    private double iva_neto;
    private double impuesto_porcentaje;
    private double impuesto_neto;
    private double ganancia_porcentaje;
    private double ganancia_neto;
    private double importe;

}
