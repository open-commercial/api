package sic.modelo;

import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(of = {"idMovimiento", "tipoComprobante", "fecha"})
public class MovimientoCaja implements Comparable<MovimientoCaja> {

    private long idMovimiento;
    private TipoDeComprobante tipoComprobante;
    private String concepto;
    private Date fecha;
    private BigDecimal monto;

    public MovimientoCaja(Recibo recibo) {
        this.idMovimiento = recibo.getIdRecibo();
        this.tipoComprobante = TipoDeComprobante.RECIBO;
        String razonSocial = ((recibo.getRazonSocialCliente().isEmpty()) ? recibo.getRazonSocialProveedor() : recibo.getRazonSocialCliente());
        this.concepto = "Recibo Nº " + recibo.getNumSerie() + " - " + recibo.getNumRecibo()
                + " del " + ((recibo.getRazonSocialCliente().isEmpty()) ? "Proveedor: " : "Cliente: ")
                + razonSocial;
        this.fecha = recibo.getFecha();
        this.monto = recibo.getRazonSocialCliente().isEmpty() ? recibo.getMonto().negate() : recibo.getMonto();
    }

    public MovimientoCaja(Gasto gasto) {
        this.idMovimiento = gasto.getId_Gasto();
        this.tipoComprobante = TipoDeComprobante.GASTO;
        this.concepto = "Gasto por: " + gasto.getConcepto();
        this.fecha = gasto.getFecha();
        this.monto = gasto.getMonto().negate();
    }

    @Override
    public int compareTo(MovimientoCaja o) {
        return o.getFecha().compareTo(this.fecha);
    }

}
