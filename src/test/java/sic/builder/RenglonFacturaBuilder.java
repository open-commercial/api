package sic.builder;

import java.math.BigDecimal;
import sic.modelo.RenglonFactura;

public class RenglonFacturaBuilder {
    
    private long idRenglonFactura = 0L;
    private long id_ProductoItem = 1L;
    private String codigoItem = "mdk.03617";
    private String descripcionItem = "Ventiladores de pie";
    private String medidaItem = "UNIDAD";
    private BigDecimal cantidad = BigDecimal.ONE;
    private BigDecimal precioUnitario = new BigDecimal("1300");
    private BigDecimal descuentoPorcentaje = BigDecimal.ZERO;
    private BigDecimal descuentoNeto = BigDecimal.ZERO;
    private BigDecimal ivaPorcentaje = new BigDecimal("21.00");
    private BigDecimal ivaNeto = new BigDecimal("273");
    private BigDecimal gananciaPorcentaje = new BigDecimal("30");
    private BigDecimal gananciaNeto = new BigDecimal("300");
    private BigDecimal importe = new BigDecimal("1573");
    
    public RenglonFactura build() {
        return new RenglonFactura(idRenglonFactura, id_ProductoItem, codigoItem,
                descripcionItem, medidaItem, cantidad, precioUnitario, descuentoPorcentaje,
                descuentoNeto, ivaPorcentaje, ivaNeto,gananciaPorcentaje, gananciaNeto, importe);
    }
    
    public RenglonFacturaBuilder withIdTransportista(long idRenglonFactura) {
        this.idRenglonFactura = idRenglonFactura;
        return this;
    }
    
    public RenglonFacturaBuilder withId_ProductoItem(long idProductoItem) {
        this.id_ProductoItem = idProductoItem;
        return this;
    }
    
    public RenglonFacturaBuilder withCodigoItem(String codigoItem) {
        this.codigoItem = codigoItem;
        return this;
    }
    
    public RenglonFacturaBuilder withDescripcionItem(String descripcionItem) {
        this.descripcionItem = descripcionItem;
        return this;
    }
    
    public RenglonFacturaBuilder withMedidaItem(String medidaItem) {
        this.medidaItem = medidaItem;
        return this;
    }
    
    public RenglonFacturaBuilder withCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
        return this;
    }
    
    public RenglonFacturaBuilder withPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
        return this;
    }

    public RenglonFacturaBuilder withDescuentoPorcentaje(BigDecimal descuentoPorcentaje) {
        this.descuentoPorcentaje = descuentoPorcentaje;
        return this;
    }
    
    public RenglonFacturaBuilder withDescuentoNeto(BigDecimal descuentoNeto) {
        this.descuentoNeto = descuentoNeto;
        return this;
    }
    
    public RenglonFacturaBuilder withIVAPorcentaje(BigDecimal ivaPorcentaje) {
        this.ivaPorcentaje = ivaPorcentaje;
        return this;
    }
    
    public RenglonFacturaBuilder withIVAneto(BigDecimal ivaNeto) {
        this.ivaNeto = ivaNeto;
        return this;
    }
    
    public RenglonFacturaBuilder withGananciaPorcentaje(BigDecimal gananciaPorcentaje) {
        this.gananciaPorcentaje = gananciaPorcentaje;
        return this;
    }
    
    public RenglonFacturaBuilder withGananciaNeto(BigDecimal gananciaNeto) {
        this.gananciaNeto = gananciaNeto;
        return this;
    }
    
    public RenglonFacturaBuilder withImporte(BigDecimal importe) {
        this.importe = importe;
        return this;
    }
    
}
