package sic.builder;

import sic.modelo.Producto;
import sic.modelo.RenglonPedido;

public class RenglonPedidoBuilder {
    
    private long id_RenglonPedido = 0L;
    private Producto producto = new ProductoBuilder().build();
    private double cantidad = 2;
    private double descuento_porcentaje = 0.0;
    private double descuento_neto = 0.0;
    private double subTotal = 363;
    
    public RenglonPedido build() {
        return new RenglonPedido(id_RenglonPedido, producto, cantidad, descuento_porcentaje, descuento_neto, subTotal);
    }
    
    public RenglonPedidoBuilder withIdRenglonPedido(long idRenglonPedido) {
        this.id_RenglonPedido = idRenglonPedido;
        return this;
    }
    
    public RenglonPedidoBuilder withProducto(Producto producto) {
        this.producto = producto;
        return this;
    }

    public RenglonPedidoBuilder withCantidad(double cantidad) {
        this.cantidad = cantidad;
        return this;
    }

    public RenglonPedidoBuilder withDescuentoPorcentaje(double descuento_porcentaje) {
        this.descuento_porcentaje = descuento_porcentaje;
        return this;
    }

    public RenglonPedidoBuilder withDescuentoNeto(double descuentoNeto) {
        this.descuento_neto = descuentoNeto;
        return this;
    }

    public RenglonPedidoBuilder withSubTotal(double subTotal) {
        this.subTotal = subTotal;
        return this;
    }
}
