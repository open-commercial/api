package sic.builder;

import java.math.BigDecimal;
import java.util.Date;
import sic.modelo.Caja;
import sic.modelo.Sucursal;
import sic.modelo.EstadoCaja;
import sic.modelo.Usuario;

public class CajaBuilder {

    private long id_Caja = 0L;
    private Date fechaApertura = new Date();
    private Date fechaCierre;
    private Usuario usuarioAbreCaja = new UsuarioBuilder().build();
    private Usuario usuarioCierraCaja = new UsuarioBuilder().build();
    private EstadoCaja estado = EstadoCaja.ABIERTA;
    private BigDecimal saldoInicial = new BigDecimal("400");
    private BigDecimal saldoSistema;
    private BigDecimal saldoReal;
    private boolean eliminada = false;
    private Sucursal sucursal = new SucursalBuilder().build();

    public Caja build() {
        return new Caja(id_Caja, fechaApertura, fechaCierre, sucursal,
                usuarioAbreCaja, usuarioCierraCaja, estado,
                saldoInicial, saldoSistema, saldoReal, eliminada);
    }

    public CajaBuilder withIdCaja(long idCaja) {
        this.id_Caja = idCaja;
        return this;
    }

    public CajaBuilder withFechaApertura(Date fechaApertura) {
        this.fechaApertura = fechaApertura;
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

    public CajaBuilder withEstado(EstadoCaja estadoCaja) {
        this.estado = estadoCaja;
        return this;
    }

    public CajaBuilder withSaldoInicial(BigDecimal saldoInicial) {
        this.saldoInicial = saldoInicial;
        return this;
    }

    public CajaBuilder withSaldoSistema(BigDecimal saldoSistema) {
        this.saldoSistema = saldoSistema;
        return this;
    }

    public CajaBuilder withSaldoReal(BigDecimal saldoReal) {
        this.saldoReal = saldoReal;
        return this;
    }

    public CajaBuilder withEliminada(boolean eliminada) {
        this.eliminada = eliminada;
        return this;
    }

    public CajaBuilder withSucursal(Sucursal sucursal) {
        this.sucursal = sucursal;
        return this;
    }
}
