package sic.builder;

import java.math.BigDecimal;
import sic.modelo.RenglonFactura;

public class RenglonFacturaBuilder {
    
    private long id_RenglonFactura = 0L;
    private long id_ProductoItem = 1L;
    private String codigoItem = "mdk.03617";
    private String descripcionItem = "Ventiladores de pie";
    private String medidaItem = "UNIDAD";
    private BigDecimal cantidad = BigDecimal.ONE;
    private BigDecimal precioUnitario = new BigDecimal(1300);
    private BigDecimal descuento_porcentaje = BigDecimal.ZERO;
    private BigDecimal descuento_neto = BigDecimal.ZERO;
    private BigDecimal iva_porcentaje = new BigDecimal(21.00);
    private BigDecimal iva_neto = new BigDecimal(273);
    private BigDecimal impuesto_porcentaje = BigDecimal.ZERO;
    private BigDecimal impuesto_neto = BigDecimal.ZERO;
    private BigDecimal ganancia_porcentaje = new BigDecimal(30);
    private BigDecimal ganancia_neto = new BigDecimal(300);
    private BigDecimal importe = new BigDecimal(1573);
    
    public RenglonFactura build() {
        return new RenglonFactura(id_RenglonFactura, id_ProductoItem, codigoItem, 
                descripcionItem, medidaItem, cantidad, precioUnitario, descuento_porcentaje, 
                descuento_neto, iva_porcentaje, iva_neto, impuesto_porcentaje, impuesto_neto,
                ganancia_porcentaje, ganancia_neto, importe);
    }
    
    public RenglonFacturaBuilder withId_Transportista(long idRenglonFactura) {
        this.id_RenglonFactura = idRenglonFactura;
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
        this.descuento_porcentaje = descuentoPorcentaje;
        return this;
    }
    
    public RenglonFacturaBuilder withDescuentoNeto(BigDecimal descuentoNeto) {
        this.descuento_neto = descuentoNeto;
        return this;
    }
    
    public RenglonFacturaBuilder withIVAPorcentaje(BigDecimal ivaPorcentaje) {
        this.iva_porcentaje = ivaPorcentaje;
        return this;
    }
    
    public RenglonFacturaBuilder withIVAneto(BigDecimal ivaNeto) {
        this.iva_neto = ivaNeto;
        return this;
    }
    
    public RenglonFacturaBuilder withImpuestoPorcentaje(BigDecimal impuestoPorcentaje) {
        this.impuesto_porcentaje = impuestoPorcentaje;
        return this;
    }
    
    public RenglonFacturaBuilder withImpuestoNeto(BigDecimal impuestoNeto) {
        this.impuesto_neto = impuestoNeto;
        return this;
    }
    
    public RenglonFacturaBuilder withGananciaPorcentaje(BigDecimal gananciaPorcentaje) {
        this.ganancia_porcentaje = gananciaPorcentaje;
        return this;
    }
    
    public RenglonFacturaBuilder withGananciaNeto(BigDecimal gananciaNeto) {
        this.ganancia_neto = gananciaNeto;
        return this;
    }
    
    public RenglonFacturaBuilder withImporte(BigDecimal importe) {
        this.importe = importe;
        return this;
    }
    
}
