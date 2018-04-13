package sic.modelo;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(name = "notacreditoproveedor")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class NotaCreditoProveedor extends NotaCredito implements Serializable {
    
    @ManyToOne
    @JoinColumn(name = "id_Proveedor", referencedColumnName = "id_Proveedor")
    private Proveedor proveedor;
    
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "id_Factura")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private FacturaCompra facturaCompra;
    
    
    public NotaCreditoProveedor() {}

    public NotaCreditoProveedor(long idNota, long serie, FacturaCompra facturaCompra, long nroNota, boolean eliminada,
            TipoDeComprobante tipoDeComprobante, Date fecha, Empresa empresa,
            Usuario usuario, String motivo, List<RenglonNotaCredito> renglones, BigDecimal subTotalBruto, BigDecimal iva21Neto,
            BigDecimal iva105Neto, BigDecimal total, long CAE, Date vencimientoCAE,
            long numSerieAfip, long numFacturaAfip, Proveedor proveedor) {

        super(idNota, serie, nroNota, eliminada, tipoDeComprobante, fecha, empresa, usuario, motivo, renglones, 
                subTotalBruto, iva21Neto, iva105Neto, total, CAE, vencimientoCAE, numSerieAfip, numFacturaAfip);
        this.facturaCompra = facturaCompra;
        this.proveedor = proveedor;
    }
}
