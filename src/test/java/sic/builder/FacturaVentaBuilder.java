package sic.builder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import sic.modelo.Cliente;
import sic.modelo.Empresa;
import sic.modelo.FacturaVenta;
import sic.modelo.Pago;
import sic.modelo.Pedido;
import sic.modelo.RenglonFactura;
import sic.modelo.TipoDeComprobante;
import sic.modelo.Transportista;
import sic.modelo.Usuario;

public class FacturaVentaBuilder {
    
    private long id_Factura = 0L;
    private Date fecha = new Date();
    private TipoDeComprobante tipoFactura = TipoDeComprobante.FACTURA_A;
    private long numSerie = 0;
    private long numFactura = 1;
    private Date fechaVencimiento = new Date();    
    private Pedido pedido =  null;
    private Transportista transportista = new TransportistaBuilder().build();
    private List<RenglonFactura> renglones;
    private List<Pago> pagos;
    private Cliente cliente = new ClienteBuilder().build();
    private Usuario usuario = new UsuarioBuilder().build();
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
      
    public FacturaVenta build() {
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
        FacturaVenta factura = new FacturaVenta(cliente, usuario, id_Factura, fecha,
                tipoFactura, numSerie, numFactura, fechaVencimiento, pedido, transportista,
                renglones, pagos, subTotal, recargo_porcentaje, recargo_neto, descuento_porcentaje, 
                descuento_neto, subTotal_neto, iva_105_neto, iva_21_neto, impuestoInterno_neto, total, 
                observaciones, pagada, empresa, eliminada, CAE, vencimientoCAE, numSerieAfip, numFacturaAfip);
        return factura;
    }
    
    public FacturaVentaBuilder withId_Factura(long idFactura) {
        this.id_Factura = idFactura;
        return this;
    }
    
    public FacturaVentaBuilder withFecha(Date fecha) {
        this.fecha = fecha;
        return this;
    }
    
    public FacturaVentaBuilder withTipoFactura(TipoDeComprobante tipoDeComprobante) {
        this.tipoFactura = tipoDeComprobante;
        return this;
    }
    
    public FacturaVentaBuilder withTransportista(Transportista transportista) {
        this.transportista = transportista;
        return this;
    }

    public FacturaVentaBuilder withPedido(Pedido pedido) {
        this.pedido = pedido;
        return this;
    }

    public FacturaVentaBuilder withPagos(List<Pago> pagos) {
        this.pagos = pagos;
        return this;
    }

    public FacturaVentaBuilder withRenglones(List<RenglonFactura> renglones) {
        this.renglones = renglones;
        return this;
    }
    
    public FacturaVentaBuilder withNumSerie(long numeroDeSerie) {
        this.numSerie = numeroDeSerie;
        return this;
    }
    
    public FacturaVentaBuilder withNumFactura(long numeroFactura) {
        this.numFactura = numeroFactura;
        return this;
    }
    
    public FacturaVentaBuilder withFechaVencimiento(Date fechaDeVencimiento) {
        this.fechaVencimiento = fechaDeVencimiento;
        return this;
    }  
    
    public FacturaVentaBuilder withCliente(Cliente cliente) {
        this.cliente = cliente;
        return this;
    }

    public FacturaVentaBuilder withUsuario(Usuario usuario) {
        this.usuario = usuario;
        return this;
    }

    public FacturaVentaBuilder withSubTotal(double subTotal) {
        this.subTotal = subTotal;
        return this;
    }

    public FacturaVentaBuilder withRecargo_porcentaje(double recargoPorcentaje) {
        this.recargo_porcentaje = recargoPorcentaje;
        return this;
    }

    public FacturaVentaBuilder withRecargo_neto(double recargoNeto) {
        this.recargo_neto = recargoNeto;
        return this;
    }

    public FacturaVentaBuilder withDescuento_porcentaje(double descuentoPorcentaje) {
        this.descuento_porcentaje = descuentoPorcentaje;
        return this;
    }

    public FacturaVentaBuilder withDescuento_neto(double descuentoNeto) {
        this.descuento_neto = descuentoNeto;
        return this;
    }

    public FacturaVentaBuilder withSubTotal_neto(double subTotalNeto) {
        this.subTotal_neto = subTotalNeto;
        return this;
    }

    public FacturaVentaBuilder withIva_105_neto(double iva105Neto) {
        this.iva_105_neto = iva105Neto;
        return this;
    }

    public FacturaVentaBuilder withIva_21_neto(double iva21Neto) {
        this.iva_21_neto = iva21Neto;
        return this;
    }

    public FacturaVentaBuilder withImpuestoInterno_neto(double impuestoInternoNeto) {
        this.impuestoInterno_neto = impuestoInternoNeto;
        return this;
    }

    public FacturaVentaBuilder withTotal(double total) {
        this.total = total;
        return this;
    }

    public FacturaVentaBuilder withObservaciones(String observarciones) {
        this.observaciones = observarciones;
        return this;
    }

    public FacturaVentaBuilder withPagada(boolean pagada) {
        this.pagada = pagada;
        return this;
    }

    public FacturaVentaBuilder withEmpresa(Empresa empresa) {
        this.empresa = empresa;
        return this;
    }

    public FacturaVentaBuilder withEliminada(boolean eliminada) {
        this.eliminada = eliminada;
        return this;
    }
    
    public FacturaVentaBuilder withCAE(long CAE) {
        this.CAE = CAE;
        return this;
    }
    
    public FacturaVentaBuilder withVencimientoCAE(Date vencimientoCAE) {
        this.vencimientoCAE = vencimientoCAE;
        return this;
    }
    
    public FacturaVentaBuilder withNumSerieAfip(long numeroDeSerieAfip) {
        this.numSerieAfip = numeroDeSerieAfip;
        return this;
    }
    
    public FacturaVentaBuilder withNumFacturaAfip(long numeroFacturaAfip) {
        this.numFacturaAfip = numeroFacturaAfip;
        return this;
    }    
}
