package sic.modelo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(name = "notacredito")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true, exclude = "renglonesNotaCredito")
public abstract class NotaCredito extends Nota implements Serializable {
    
    @Column(nullable = false)
    private boolean modificaStock;
    
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "idNota")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false)
    private List<RenglonNotaCredito> renglonesNotaCredito;
    
    @Column(precision = 25, scale = 15)
    private BigDecimal subTotal;
    
    @Column(precision = 25, scale = 15)
    private BigDecimal recargoPorcentaje;
    
    @Column(precision = 25, scale = 15)
    private BigDecimal recargoNeto;
    
    @Column(precision = 25, scale = 15)
    private BigDecimal descuentoPorcentaje;
    
    @Column(precision = 25, scale = 15)
    private BigDecimal descuentoNeto;

    public NotaCredito() {}

    public NotaCredito(long idNota, long serie, long nroNota, boolean eliminada,
            TipoDeComprobante tipoDeComprobante, Date fecha, Empresa empresa,
            Usuario usuario, Cliente cliente, FacturaVenta facturaVenta, String motivo, List<RenglonNotaCredito> renglones, BigDecimal subTotalBruto, BigDecimal iva21Neto,
            BigDecimal iva105Neto, BigDecimal total, long CAE, Date vencimientoCAE,
            long numSerieAfip, long numFacturaAfip) {

        super(idNota, serie, nroNota, eliminada, tipoDeComprobante, fecha, empresa, usuario, cliente, facturaVenta, null, null, motivo,
                subTotalBruto, iva21Neto, iva105Neto, total, CAE, vencimientoCAE, numSerieAfip, numFacturaAfip);
        this.renglonesNotaCredito = renglones;
    }

    public NotaCredito(long idNota, long serie, long nroNota, boolean eliminada,
                       TipoDeComprobante tipoDeComprobante, Date fecha, Empresa empresa,
                       Usuario usuario, Proveedor proveedor, FacturaCompra facturaCompra, String motivo, List<RenglonNotaCredito> renglones, BigDecimal subTotalBruto, BigDecimal iva21Neto,
                       BigDecimal iva105Neto, BigDecimal total, long CAE, Date vencimientoCAE,
                       long numSerieAfip, long numFacturaAfip) {

        super(idNota, serie, nroNota, eliminada, tipoDeComprobante, fecha, empresa, usuario, null, null, proveedor, facturaCompra, motivo,
                subTotalBruto, iva21Neto, iva105Neto, total, CAE, vencimientoCAE, numSerieAfip, numFacturaAfip);
        this.renglonesNotaCredito = renglones;
    }

}
