package sic.modelo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

public class NotaDebitoProveedor extends NotaDebito implements Serializable {
    
    @ManyToOne
    @JoinColumn(name = "id_Proveedor", referencedColumnName = "id_Proveedor")
    private Proveedor proveedor;

    public NotaDebitoProveedor() {
    }

    public NotaDebitoProveedor(long idNota, long serie, FacturaVenta facturaVenta, List<Pago> pagos, long nroNota, boolean eliminada,
            TipoDeComprobante tipoDeComprobante, Date fecha, Empresa empresa, Usuario usuario, String motivo, List<RenglonNotaDebito> renglones,
            BigDecimal subTotalBruto, BigDecimal iva21Neto, BigDecimal iva105Neto, BigDecimal total, BigDecimal montoNoGravado, long CAE,
            Date vencimientoCAE, long numSerieAfip, long numNotaAfip, Recibo recibo, boolean pagado, Proveedor proveedor) {

        super(idNota, serie, facturaVenta, pagos, nroNota, eliminada, tipoDeComprobante, fecha, empresa, usuario, motivo, renglones,
                subTotalBruto, iva21Neto, iva105Neto, total, montoNoGravado, CAE, vencimientoCAE, numSerieAfip, numNotaAfip, recibo, pagado);
        this.proveedor = proveedor;
    }

}
