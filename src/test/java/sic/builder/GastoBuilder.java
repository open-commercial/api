package sic.builder;

import java.util.Date;
import sic.modelo.Empresa;
import sic.modelo.FormaDePago;
import sic.modelo.Gasto;
import sic.modelo.Usuario;

public class GastoBuilder {

    private long id_Gasto = 0L;
    private long nroGasto = 34;
    private Date fecha = new Date();
    private String concepto = "Gasto Factura de Luz - Builder";
    private Empresa empresa = new EmpresaBuilder().build();
    private Usuario usuario = new UsuarioBuilder().build();
    private FormaDePago formaDePago = new FormaDePagoBuilder().build();
    private double monto = 245.33;
    private boolean eliminado = false;

    public Gasto build() {
        return new Gasto(id_Gasto, nroGasto, fecha, concepto, empresa, usuario, formaDePago, monto, eliminado);
    }

    public GastoBuilder withIdGasto(long idGasto) {
        this.id_Gasto = idGasto;
        return this;
    }

    public GastoBuilder withNroGasto(long nroGasto) {
        this.nroGasto = nroGasto;
        return this;
    }

    public GastoBuilder withFecha(Date fecha) {
        this.fecha = fecha;
        return this;
    }

    public GastoBuilder withConcepto(String concepto) {
        this.concepto = concepto;
        return this;
    }

    public GastoBuilder withEmpresa(Empresa empresa) {
        this.empresa = empresa;
        return this;
    }

    public GastoBuilder withUsuario(Usuario usuario) {
        this.usuario = usuario;
        return this;
    }

    public GastoBuilder withFormaDePago(FormaDePago formaDePago) {
        this.formaDePago = formaDePago;
        return this;
    }

    public GastoBuilder withMonto(double monto) {
        this.monto = monto;
        return this;
    }

    public GastoBuilder withEliminado(boolean eliminado) {
        this.eliminado = eliminado;
        return this;
    }

}
