package sic.builder;

import java.math.BigDecimal;
import java.util.Date;
import sic.modelo.Empresa;
import sic.modelo.Factura;
import sic.modelo.FormaDePago;
import sic.modelo.Pago;
import sic.modelo.Recibo;

public class PagoBuilder {

    private Long id_Pago = 0L;
    private long nroPago = 1L;
    private FormaDePago formaDePago = new FormaDePagoBuilder().build();
    private Factura factura = new FacturaVentaBuilder().build();
    private BigDecimal monto = new BigDecimal(100);
    private Date fecha = new Date();
    private String nota = "Pago por 100 pesos";
    private Recibo recibo = new ReciboBuilder().build();
    private Empresa empresa = new EmpresaBuilder().build();
    private boolean eliminado = false; 
    
    public Pago build() {
      return new Pago(id_Pago, nroPago, formaDePago, factura, null, recibo, monto, fecha, nota, empresa, eliminado);
    }

    public PagoBuilder withId_Pago(Long idPago) {
        this.id_Pago = idPago;
        return this;
    } 
    
    public PagoBuilder withNroPago(long nroPago) {
       this.nroPago = nroPago;
       return this;
    }
    
    public PagoBuilder withFormaDePago(FormaDePago formaDePago) {
        this.formaDePago = formaDePago;
        return this;
    }
    
    public PagoBuilder withFactura(Factura factura) {
        this.factura = factura;
        return this;
    }
    
    public PagoBuilder withRecibo(Recibo recibo) {
        this.recibo = recibo;
        return this;
    }

    public PagoBuilder withMonto(BigDecimal monto) {
        this.monto = monto;
        return this;
    }

    public PagoBuilder withFecha(Date fecha) {
        this.fecha = fecha;
        return this;
    }

    public PagoBuilder withNota(String nota) {
        this.nota = nota;
        return this;
    }

    public PagoBuilder withEmpresa(Empresa empresa) {
        this.empresa = empresa;
        return this;
    }

    public PagoBuilder withEliminado(boolean eliminado) {
        this.eliminado = eliminado;
        return this;
    }

}
