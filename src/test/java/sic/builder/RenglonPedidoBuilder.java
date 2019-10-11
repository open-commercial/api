package sic.builder;

import java.math.BigDecimal;

import sic.modelo.dto.RenglonPedidoDTO;

public class RenglonPedidoBuilder {
    
    private long id_RenglonPedido = 0L;
    private long idProductoItem = 1L;
    private String codigoItem = "ABC123";
    private String descripcionItem = "Cinta adhesiva doble faz 3M";
    private String medidaItem = "Metro";
    private String urlImagenItem = "https://www.imagen.com";
    private boolean oferta = false;
    private BigDecimal precioUnitario = new BigDecimal("181.5");
    private BigDecimal cantidad = new BigDecimal("2");
    private BigDecimal bonificacionPorcentaje = BigDecimal.ZERO;
    private BigDecimal bonificacionNeta = BigDecimal.ZERO;
    private BigDecimal subTotal = new BigDecimal("363");
    private BigDecimal importeAnterior = new BigDecimal("363");
    private BigDecimal importe = new BigDecimal("363");

  public RenglonPedidoDTO build() {
    return new RenglonPedidoDTO(
        id_RenglonPedido,
        idProductoItem,
        codigoItem,
        descripcionItem,
        medidaItem,
        urlImagenItem,
        oferta,
        precioUnitario,
        cantidad,
        bonificacionPorcentaje,
        bonificacionNeta,
        subTotal,
        importeAnterior,
        importe);
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

    public RenglonPedidoBuilder withMedidaItem(String medidaItem) {
        this.medidaItem = medidaItem;
        return this;
    }

    public RenglonPedidoBuilder withUrlImagenItem(String urlImagenItem) {
        this.urlImagenItem = urlImagenItem;
        return this;
    }

    public RenglonPedidoBuilder withOferta(boolean oferta) {
        this.oferta = oferta;
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

    public RenglonPedidoBuilder withSubTotal(BigDecimal subTotal) {
        this.subTotal = subTotal;
        return this;
    }

    public RenglonPedidoBuilder withBonificacionPorcentaje(BigDecimal bonificacionPorcentaje) {
        this.bonificacionPorcentaje = bonificacionPorcentaje;
        return this;
    }

    public RenglonPedidoBuilder withBonificacionNeta(BigDecimal bonificacionNeta) {
        this.bonificacionNeta = bonificacionNeta;
        return this;
    }

    public RenglonPedidoBuilder withImporteAnterior(BigDecimal importeAnterior) {
        this.importeAnterior = importeAnterior;
        return this;
    }

    public RenglonPedidoBuilder withImporte(BigDecimal importe) {
        this.importe = importe;
        return this;
    }

}
