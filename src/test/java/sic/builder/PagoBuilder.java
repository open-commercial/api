package sic.builder;

import java.util.Date;
import sic.modelo.Empresa;
import sic.modelo.Factura;
import sic.modelo.FormaDePago;
import sic.modelo.Pago;

public class PagoBuilder {

    private Long id_Pago = 0L;
    private long nroPago = 1L;
    private FormaDePago formaDePago = new FormaDePagoBuilder().build();
    private Factura factura = new FacturaVentaBuilder().build();
    private double monto = 100;
    private Date fecha = new Date();
    private String nota = "Pago por 100 pesos";
    private Empresa empresa = new EmpresaBuilder().build();
    private boolean eliminado = false; 
    
    public Pago build() {
      return new Pago(id_Pago, nroPago, formaDePago, factura, null, monto, fecha, nota, empresa, eliminado);
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

    public PagoBuilder withMonto(double monto) {
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
