package sic.builder;

import java.math.BigDecimal;
import sic.modelo.dto.ProductoDTO;
import sic.modelo.dto.RenglonPedidoDTO;

public class RenglonPedidoBuilder {
    
    private long id_RenglonPedido = 0L;
    private ProductoDTO producto = new ProductoBuilder().build();
    private long idProducto = 1L;
    private String codigo = "ABC123";
    private String descripcion = "Cinta adhesiva doble faz 3M";
    private BigDecimal precioDeLista = new BigDecimal("181.5");
    private BigDecimal cantidad = new BigDecimal("2");
    private BigDecimal descuento_porcentaje = BigDecimal.ZERO;
    private BigDecimal descuento_neto = BigDecimal.ZERO;
    private BigDecimal subTotal = new BigDecimal("363");
    
    public RenglonPedidoDTO build() {
        return new RenglonPedidoDTO(id_RenglonPedido, producto, idProducto, codigo, descripcion, precioDeLista,
          cantidad, descuento_porcentaje, descuento_neto, subTotal);
    }
    
    public RenglonPedidoBuilder withIdRenglonPedido(long idRenglonPedido) {
        this.id_RenglonPedido = idRenglonPedido;
        return this;
    }

    public RenglonPedidoBuilder withProducto(ProductoDTO producto) {
        this.producto = producto;
        return this;
    }
    
    public RenglonPedidoBuilder withIdProducto(long idProducto) {
        this.idProducto = idProducto;
        return this;
    }

    public RenglonPedidoBuilder withCodigo(String codigo) {
        this.codigo = codigo;
        return this;
    }

    public RenglonPedidoBuilder withDescripcion(String descripcion) {
        this.descripcion = descripcion;
        return this;
    }

    public RenglonPedidoBuilder withPrecioDeLista(BigDecimal precioDeLista) {
        this.precioDeLista = precioDeLista;
        return this;
    }

    public RenglonPedidoBuilder withCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
        return this;
    }

    public RenglonPedidoBuilder withDescuentoPorcentaje(BigDecimal descuento_porcentaje) {
        this.descuento_porcentaje = descuento_porcentaje;
        return this;
    }

    public RenglonPedidoBuilder withDescuentoNeto(BigDecimal descuentoNeto) {
        this.descuento_neto = descuentoNeto;
        return this;
    }

    public RenglonPedidoBuilder withSubTotal(BigDecimal subTotal) {
        this.subTotal = subTotal;
        return this;
    }
}
