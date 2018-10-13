package sic.builder;

import java.math.BigDecimal;

import sic.modelo.dto.RenglonPedidoDTO;

public class RenglonPedidoBuilder {
    
    private long id_RenglonPedido = 0L;
    private long idProductoItem = 1L;
    private String codigoItem = "ABC123";
    private String descripcionItem = "Cinta adhesiva doble faz 3M";
    private String medidaItem = "Metro";
    private BigDecimal precioUnitario = new BigDecimal("181.5");
    private BigDecimal cantidad = new BigDecimal("2");
    private BigDecimal descuentoPorcentaje = BigDecimal.ZERO;
    private BigDecimal descuentoNeto = BigDecimal.ZERO;
    private BigDecimal subTotal = new BigDecimal("363");

    public RenglonPedidoDTO build() {
        return new RenglonPedidoDTO(id_RenglonPedido, idProductoItem, codigoItem, descripcionItem, medidaItem,
                precioUnitario, cantidad, descuentoPorcentaje, descuentoNeto, subTotal);
    }
    
    public RenglonPedidoBuilder withIdRenglonPedido(long idRenglonPedido) {
        this.id_RenglonPedido = idRenglonPedido;
        return this;
    }
    
    public RenglonPedidoBuilder withIdProducto(long idProducto) {
        this.idProductoItem = idProducto;
        return this;
    }

    public RenglonPedidoBuilder withCodigo(String codigo) {
        this.codigoItem = codigo;
        return this;
    }

    public RenglonPedidoBuilder withDescripcion(String descripcion) {
        this.descripcionItem = descripcion;
        return this;
    }

    public RenglonPedidoBuilder withPrecioDeLista(BigDecimal precioDeLista) {
        this.precioUnitario = precioDeLista;
        return this;
    }

    public RenglonPedidoBuilder withCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
        return this;
    }

    public RenglonPedidoBuilder withDescuentoPorcentaje(BigDecimal descuento_porcentaje) {
        this.descuentoPorcentaje = descuento_porcentaje;
        return this;
    }

    public RenglonPedidoBuilder withDescuentoNeto(BigDecimal descuentoNeto) {
        this.descuentoNeto = descuentoNeto;
        return this;
    }

    public RenglonPedidoBuilder withSubTotal(BigDecimal subTotal) {
        this.subTotal = subTotal;
        return this;
    }
}
