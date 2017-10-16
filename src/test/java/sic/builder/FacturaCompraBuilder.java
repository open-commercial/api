package sic.builder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import sic.modelo.Empresa;
import sic.modelo.FacturaCompra;
import sic.modelo.Pago;
import sic.modelo.Pedido;
import sic.modelo.Proveedor;
import sic.modelo.RenglonFactura;
import sic.modelo.TipoDeComprobante;
import sic.modelo.Transportista;

public class FacturaCompraBuilder {
    
    private long id_Factura = 0L;
    private Date fecha = new Date();
    private TipoDeComprobante tipoFactura = TipoDeComprobante.FACTURA_A;
    private long numSerie = 0;
    private long numFactura = 1;
    private Date fechaVencimiento = new Date();
    private Pedido pedido = null;
    private Transportista transportista = new TransportistaBuilder().build();
    private List<RenglonFactura> renglones;
    private List<Pago> pagos;
    private Proveedor proveedor = new ProveedorBuilder().build();
    private double subTotal = 6500;
    private double recargo_porcentaje = 0.0;
    private double recargo_neto = 0.0;
    private double descuento_porcentaje = 0.0;
    private double descuento_neto = 0.0;
    private double subTotal_neto = 6500;
    private double iva_105_neto = 0.0;
    private double iva_21_neto = 1365;
    private double impuestoInterno_neto = 0.0;
    private double total = 7865;
    private String observaciones = "Factura por Default";
    private boolean pagada = false;
    private Empresa empresa = new EmpresaBuilder().build();
    private boolean eliminada = false;
    private long CAE = 21064126523746l;
    private Date vencimientoCAE = null;
    private long numSerieAfip = 0;
    private long numFacturaAfip = 0;
    
    public FacturaCompra build() {
        if (renglones == null) {
            RenglonFactura renglon1 = new RenglonFacturaBuilder().build();
            RenglonFactura renglon2 = new RenglonFacturaBuilder()
                                            .withCantidad(4)
                                            .withId_ProductoItem(890L)
                                            .withCodigoItem("mate.0923")
                                            .withIVAneto(1092)
                                            .withPrecioUnitario(5200)
                                            .build();
            List<RenglonFactura> renglonesFactura = new ArrayList<>();
            renglonesFactura.add(renglon1);
            renglonesFactura.add(renglon2);
            this.renglones = renglonesFactura;
        }
        FacturaCompra factura = new FacturaCompra(id_Factura, fecha, tipoFactura, 
                numSerie, numFactura, fechaVencimiento, pedido, transportista, renglones, 
                pagos, subTotal, recargo_porcentaje, recargo_neto, descuento_porcentaje, descuento_neto, 
                subTotal_neto, iva_105_neto, iva_21_neto, impuestoInterno_neto, total, observaciones, 
                pagada, empresa, eliminada, CAE, vencimientoCAE, proveedor, numSerieAfip, numFacturaAfip);
        return factura;
    }
    
    public FacturaCompraBuilder withId_Factura(long idFactura) {
        this.id_Factura = idFactura;
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

    public FacturaCompraBuilder withPagos(List<Pago> pagos) {
        this.pagos = pagos;
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

    public FacturaCompraBuilder withSubTotal(double subTotal) {
        this.subTotal = subTotal;
        return this;
    }

    public FacturaCompraBuilder withRecargo_porcentaje(double recargoPorcentaje) {
        this.recargo_porcentaje = recargoPorcentaje;
        return this;
    }

    public FacturaCompraBuilder withRecargo_neto(double recargoNeto) {
        this.recargo_neto = recargoNeto;
        return this;
    }

    public FacturaCompraBuilder withDescuento_porcentaje(double descuentoPorcentaje) {
        this.descuento_porcentaje = descuentoPorcentaje;
        return this;
    }

    public FacturaCompraBuilder withDescuento_neto(double descuentoNeto) {
        this.descuento_neto = descuentoNeto;
        return this;
    }

    public FacturaCompraBuilder withSubTotal_neto(double subTotalNeto) {
        this.subTotal_neto = subTotalNeto;
        return this;
    }

    public FacturaCompraBuilder withIva_105_neto(double iva105Neto) {
        this.iva_105_neto = iva105Neto;
        return this;
    }

    public FacturaCompraBuilder withIva_21_neto(double iva21Neto) {
        this.iva_21_neto = iva21Neto;
        return this;
    }

    public FacturaCompraBuilder withImpuestoInterno_neto(double impuestoInternoNeto) {
        this.impuestoInterno_neto = impuestoInternoNeto;
        return this;
    }

    public FacturaCompraBuilder withTotal(double total) {
        this.total = total;
        return this;
    }

    public FacturaCompraBuilder withObservaciones(String observarciones) {
        this.observaciones = observarciones;
        return this;
    }

    public FacturaCompraBuilder withPagada(boolean pagada) {
        this.pagada = pagada;
        return this;
    }

    public FacturaCompraBuilder withEmpresa(Empresa empresa) {
        this.empresa = empresa;
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

