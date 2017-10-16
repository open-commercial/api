package sic.builder;

import java.util.Date;
import java.util.Map;
import sic.modelo.Caja;
import sic.modelo.Empresa;
import sic.modelo.EstadoCaja;
import sic.modelo.Usuario;

public class CajaBuilder {

    private long id_Caja = 0L;
    private int nroCaja = 22;
    private Date fechaApertura = new Date();
    private Date fechaCorteInforme = new Date();
    private Date fechaCierre;
    private Usuario usuarioAbreCaja = new UsuarioBuilder().build();
    private Usuario usuarioCierraCaja = new UsuarioBuilder().build();
    private String observacion = "Caja Default para Test";
    private EstadoCaja estado = EstadoCaja.ABIERTA;
    private double saldoInicial = 400;
    private double saldoFinal;
    private double saldoReal;
    private boolean eliminada = false;
    private Map<Long, Double> totalesPorFomaDePago;
    private double totalAfectaCaja;
    private double totalGeneral;
    private Empresa empresa = new EmpresaBuilder().build();

    public Caja build() {
        return new Caja(id_Caja, nroCaja, fechaApertura, fechaCorteInforme, fechaCierre, empresa,
                usuarioAbreCaja, usuarioCierraCaja, observacion, estado,
                saldoInicial, saldoFinal, saldoReal, eliminada, totalesPorFomaDePago, totalAfectaCaja, totalGeneral);
    }

    public CajaBuilder withIdCaja(long idCaja) {
        this.id_Caja = idCaja;
        return this;
    }

    public CajaBuilder withNroCaja(int nroCaja) {
        this.nroCaja = nroCaja;
        return this;
    }

    public CajaBuilder withFechaApertura(Date fechaApertura) {
        this.fechaApertura = fechaApertura;
        return this;
    }

    public CajaBuilder withFechaCorteInforme(Date fechaCorteInforme) {
        this.fechaCorteInforme = fechaCorteInforme;
        return this;
    }

    public CajaBuilder withFechaCierre(Date fechaCierre) {
        this.fechaCierre = fechaCierre;
        return this;
    }

    public CajaBuilder withUsuarioAbreCaja(Usuario usuarioAbreCaja) {
        this.usuarioAbreCaja = usuarioAbreCaja;
        return this;
    }

    public CajaBuilder withUsuarioCierraCaja(Usuario usuarioCierraCaja) {
        this.usuarioCierraCaja = usuarioCierraCaja;
        return this;
    }

    public CajaBuilder withObservacion(String observacion) {
        this.observacion = observacion;
        return this;
    }

    public CajaBuilder withEstado(EstadoCaja estadoCaja) {
        this.estado = estadoCaja;
        return this;
    }

    public CajaBuilder withSaldoInicial(double saldoInicial) {
        this.saldoInicial = saldoInicial;
        return this;
    }

    public CajaBuilder withSaldoFinal(double saldoFinal) {
        this.saldoFinal = saldoFinal;
        return this;
    }

    public CajaBuilder withSaldoReal(double saldoReal) {
        this.saldoReal = saldoReal;
        return this;
    }

    public CajaBuilder withEliminada(boolean eliminada) {
        this.eliminada = eliminada;
        return this;
    }

    public CajaBuilder withEmpresa(Empresa empresa) {
        this.empresa = empresa;
        return this;
    }
}
