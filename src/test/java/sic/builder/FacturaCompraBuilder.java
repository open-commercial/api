package sic.builder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import sic.modelo.*;

public class FacturaCompraBuilder {
    
    private long id_Factura = 0L;
    private Usuario usuario = new UsuarioBuilder().build();
    private Date fecha = new Date();
    private TipoDeComprobante tipoFactura = TipoDeComprobante.FACTURA_A;
    private long numSerie = 0;
    private long numFactura = 1;
    private Date fechaVencimiento = new Date();
    private Pedido pedido = null;
    private Transportista transportista = new TransportistaBuilder().build();
    private List<RenglonFactura> renglones;
    private Proveedor proveedor = new ProveedorBuilder().build();
    private BigDecimal subTotal = new BigDecimal("6500");
    private BigDecimal recargo_porcentaje = BigDecimal.ZERO;
    private BigDecimal recargo_neto = BigDecimal.ZERO;
    private BigDecimal descuento_porcentaje = BigDecimal.ZERO;
    private BigDecimal descuento_neto = BigDecimal.ZERO;
    private BigDecimal subTotal_neto = new BigDecimal("6500");
    private BigDecimal iva_105_neto = BigDecimal.ZERO;
    private BigDecimal iva_21_neto = new BigDecimal("1365");
    private BigDecimal impuestoInterno_neto = BigDecimal.ZERO;
    private BigDecimal total = new BigDecimal("7865");
    private String observaciones = "Factura por Default";
    private BigDecimal cantidadArticulos = new BigDecimal("5");
    private Sucursal sucursal = new SucursalBuilder().build();
    private boolean eliminada = false;
    private long CAE = 21064126523746l;
    private Date vencimientoCAE = null;
    private long numSerieAfip = 0;
    private long numFacturaAfip = 0;
    
    public FacturaCompra build() {
        if (renglones == null) {
            RenglonFactura renglon1 = new RenglonFacturaBuilder().build();
            RenglonFactura renglon2 = new RenglonFacturaBuilder()
                                            .withCantidad(new BigDecimal("4"))
                                            .withId_ProductoItem(890L)
                                            .withCodigoItem("mate.0923")
                                            .withIVAneto(new BigDecimal("1092"))
                                            .withPrecioUnitario(new BigDecimal("5200"))
                                            .build();
            List<RenglonFactura> renglonesFactura = new ArrayList<>();
            renglonesFactura.add(renglon1);
            renglonesFactura.add(renglon2);
            this.renglones = renglonesFactura;
        }
        FacturaCompra factura = new FacturaCompra(id_Factura, usuario, fecha, tipoFactura,
                numSerie, numFactura, fechaVencimiento, pedido, transportista, renglones,
                subTotal, recargo_porcentaje, recargo_neto, descuento_porcentaje, descuento_neto,
                subTotal_neto, iva_105_neto, iva_21_neto, impuestoInterno_neto, total, observaciones,
                cantidadArticulos, sucursal, eliminada, CAE, vencimientoCAE, proveedor, numSerieAfip, numFacturaAfip);
        return factura;
    }
    
    public FacturaCompraBuilder withId_Factura(long idFactura) {
        this.id_Factura = idFactura;
        return this;
    }

    public FacturaCompraBuilder withUsuario(Usuario usuario) {
        this.usuario = usuario;
        return this;
    }
    
    public FacturaCompraBuilder withFecha(Date fecha) {
        this.fecha = fecha;
        return this;
    }
    
    public FacturaCompraBuilder withTipoFactura(TipoDeComprobante tipoDeComprobante) {
        this.tipoFactura = tipoDeComprobante;
        return this;
    }
    
    public FacturaCompraBuilder withTransportista(Transportista transportista) {
        this.transportista = transportista;
        return this;
    }

    public FacturaCompraBuilder withPedido(Pedido pedido) {
        this.pedido = pedido;
        return this;
    }

    public FacturaCompraBuilder withRenglones(List<RenglonFactura> renglones) {
        this.renglones = renglones;
        return this;
    }
    
    public FacturaCompraBuilder withNumSerie(long numeroDeSerie) {
        this.numSerie = numeroDeSerie;
        return this;
    }
    
    public FacturaCompraBuilder withNumFactura(long numeroFactura) {
        this.numFactura = numeroFactura;
        return this;
    }
    
    public FacturaCompraBuilder withFechaVencimiento(Date fechaDeVencimiento) {
        this.fechaVencimiento = fechaDeVencimiento;
        return this;
    }  

    public FacturaCompraBuilder withUsuario(Proveedor proveedor) {
        this.proveedor = proveedor;
        return this;
    }

    public FacturaCompraBuilder withSubTotal(BigDecimal subTotal) {
        this.subTotal = subTotal;
        return this;
    }

    public FacturaCompraBuilder withRecargo_porcentaje(BigDecimal recargoPorcentaje) {
        this.recargo_porcentaje = recargoPorcentaje;
        return this;
    }

    public FacturaCompraBuilder withRecargo_neto(BigDecimal recargoNeto) {
        this.recargo_neto = recargoNeto;
        return this;
    }

    public FacturaCompraBuilder withDescuento_porcentaje(BigDecimal descuentoPorcentaje) {
        this.descuento_porcentaje = descuentoPorcentaje;
        return this;
    }

    public FacturaCompraBuilder withDescuento_neto(BigDecimal descuentoNeto) {
        this.descuento_neto = descuentoNeto;
        return this;
    }

    public FacturaCompraBuilder withSubTotal_neto(BigDecimal subTotalNeto) {
        this.subTotal_neto = subTotalNeto;
        return this;
    }

    public FacturaCompraBuilder withIva_105_neto(BigDecimal iva105Neto) {
        this.iva_105_neto = iva105Neto;
        return this;
    }

    public FacturaCompraBuilder withIva_21_neto(BigDecimal iva21Neto) {
        this.iva_21_neto = iva21Neto;
        return this;
    }

    public FacturaCompraBuilder withImpuestoInterno_neto(BigDecimal impuestoInternoNeto) {
        this.impuestoInterno_neto = impuestoInternoNeto;
        return this;
    }

    public FacturaCompraBuilder withTotal(BigDecimal total) {
        this.total = total;
        return this;
    }

    public FacturaCompraBuilder withObservaciones(String observarciones) {
        this.observaciones = observarciones;
        return this;
    }

    public FacturaCompraBuilder withCantidadArticulos(BigDecimal cantidadArticulos) {
        this.cantidadArticulos = cantidadArticulos;
        return this;
    }

    public FacturaCompraBuilder withSucursal(Sucursal sucursal) {
        this.sucursal = sucursal;
        return this;
    }

    public FacturaCompraBuilder withEliminada(boolean eliminada) {
        this.eliminada = eliminada;
        return this;
    }
    
    public FacturaCompraBuilder withCAE(long CAE) {
        this.CAE = CAE;
        return this;
    }
    
    public FacturaCompraBuilder withVencimientoCAE(Date vencimientoCAE) {
        this.vencimientoCAE = vencimientoCAE;
        return this;
    }
    
    public FacturaCompraBuilder withNumSerieAfip(long numeroDeSerieAfip) {
        this.numSerieAfip = numeroDeSerieAfip;
        return this;
    }
    
    public FacturaCompraBuilder withNumFacturaAfip(long numeroFacturaAfip) {
        this.numFacturaAfip = numeroFacturaAfip;
        return this;
    }    
}

