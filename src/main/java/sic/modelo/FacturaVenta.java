package sic.modelo;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(name = "facturaventa")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@JsonIgnoreProperties({"cliente", "usuario", "empresa", "pedido", "transportista"})
public class FacturaVenta extends Factura implements Serializable {

    @ManyToOne
    @JoinColumn(name = "id_Cliente", referencedColumnName = "id_Cliente")
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "id_Usuario", referencedColumnName = "id_Usuario")
    private Usuario usuario;

    public FacturaVenta() {}

    public FacturaVenta(Cliente cliente, Usuario usuario, long id_Factura, Date fecha,
            TipoDeComprobante tipoComprobante, long numSerie, long numFactura, Date fechaVencimiento,
            Pedido pedido, Transportista transportista, List<RenglonFactura> renglones,
            List<Pago> pagos, BigDecimal subTotal, BigDecimal recargo_porcentaje, BigDecimal recargo_neto,
            BigDecimal descuento_porcentaje, BigDecimal descuento_neto, BigDecimal subTotal_neto, BigDecimal iva_105_neto,
            BigDecimal iva_21_neto, BigDecimal impuestoInterno_neto, BigDecimal total, String observaciones, boolean pagada,
            Empresa empresa, boolean eliminada, long CAE, Date vencimientoCAE, long numSerieAfip, long numFacturaAfip) {
        
        super(id_Factura, fecha, tipoComprobante, numSerie, numFactura, fechaVencimiento, 
                pedido, transportista, renglones, pagos, subTotal, recargo_porcentaje, 
                recargo_neto, descuento_porcentaje, descuento_neto, subTotal_neto, 
                iva_105_neto, iva_21_neto, impuestoInterno_neto, total, observaciones,
                pagada, empresa, eliminada, CAE, vencimientoCAE, numSerieAfip, numFacturaAfip);
        this.cliente = cliente;
        this.usuario = usuario;
    }
    
    @JsonGetter("razonSocialCliente")
    public String getRazonSocialCliente() {
        return cliente.getRazonSocial();
    }    
    
    @JsonGetter("nombreUsuario")
    public String getNombreUsuario() {
        return usuario.getNombre();
    }

}
