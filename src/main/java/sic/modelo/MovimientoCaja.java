package sic.modelo;

import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sic.controller.Views;

@Data
@EqualsAndHashCode(of = {"idMovimiento", "tipoComprobante", "fecha"})
@JsonView(Views.Comprador.class)
public class MovimientoCaja implements Comparable<MovimientoCaja> {

    private long idMovimiento;
    private TipoDeComprobante tipoComprobante;
    private String concepto;
    private Date fecha;
    private BigDecimal monto;

    public MovimientoCaja(Recibo recibo) {
        this.idMovimiento = recibo.getIdRecibo();
        this.tipoComprobante = TipoDeComprobante.RECIBO;
        String razonSocial = ((recibo.getNombreFiscalCliente().isEmpty()) ? recibo.getRazonSocialProveedor() : recibo.getNombreFiscalCliente());
        this.concepto = "Recibo Nº " + recibo.getNumSerie() + " - " + recibo.getNumRecibo()
                + " del " + ((recibo.getNombreFiscalCliente().isEmpty()) ? "Proveedor: " : "Cliente: ")
                + razonSocial;
        this.fecha = recibo.getFecha();
        this.monto = recibo.getNombreFiscalCliente().isEmpty() ? recibo.getMonto().negate() : recibo.getMonto();
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
