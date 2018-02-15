package sic.builder;

import java.math.BigDecimal;
import sic.modelo.RenglonNotaCredito;

public class RenglonNotaCreditoBuilder {
    
    private long id_RenglonNota = 1L;
    private long id_ProductoItem = 1L;
    private String codigoItem = "1";
    private String descripcionItem = "Acero Valyrio";
    private String medidaItem = "Kilos";
    private BigDecimal descuentoPorcentaje = BigDecimal.TEN;
    private BigDecimal descuentoNeto = new BigDecimal("15.8");
    private BigDecimal cantidad = new BigDecimal("2");  
    private BigDecimal precioUnitario = new BigDecimal("100");
    private BigDecimal ivaPorcentaje =  new BigDecimal("21");
    private BigDecimal ivaNeto;
    private BigDecimal importe  = new BigDecimal("158"); //sin nada
    private BigDecimal importeBruto = new BigDecimal("142.2");  //con descuentos y recargos, sin iva
    private BigDecimal importeNeto = new BigDecimal("172.062"); //con descuentos, recargos y con iva
    
    public RenglonNotaCredito build() {
        return new RenglonNotaCredito(id_RenglonNota, id_ProductoItem, codigoItem, descripcionItem, medidaItem, cantidad, precioUnitario, ivaPorcentaje, 
                                      ivaNeto, importe, descuentoPorcentaje, descuentoNeto, importeBruto, ivaPorcentaje, ivaNeto, importeNeto);
    }
    
    public RenglonNotaCreditoBuilder withId_RenglonNota(long idRenglonNota) {
        this.id_RenglonNota = idRenglonNota;
        return this;
    }
    
    public RenglonNotaCreditoBuilder withId_ProductoItem(long id_ProductoItem) {
        this.id_ProductoItem = id_ProductoItem;
        return this;
    }
    
    public RenglonNotaCreditoBuilder withCodigoItem(String codigoItem) {
        this.codigoItem = codigoItem;
        return this;
    }
    
    public RenglonNotaCreditoBuilder withDescripcionItem(String descripcionItem) {
        this.descripcionItem = descripcionItem;
        return this;
    }
    
    public RenglonNotaCreditoBuilder withMedidaItem(String medidaItem) {
        this.medidaItem = medidaItem;
        return this;
    }
    
    public RenglonNotaCreditoBuilder withDescuentoPorcentaje(BigDecimal descuentoPorcentaje) {
        this.descuentoPorcentaje = descuentoPorcentaje;
        return this;
    }
    
    public RenglonNotaCreditoBuilder withDescuentoNeto(BigDecimal descuentoNeto) {
        this.descuentoNeto = descuentoNeto;
        return this;
    }
    
    public RenglonNotaCreditoBuilder withCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
        return this;
    }
    
    public RenglonNotaCreditoBuilder withPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
        return this;
    }
    
    public RenglonNotaCreditoBuilder withIvaNeto(BigDecimal ivaNeto) {
        this.ivaNeto = ivaNeto;
        return this;
    }
    
    public RenglonNotaCreditoBuilder withIvaPorcentaje(BigDecimal ivaPorcentaje) {
        this.ivaPorcentaje = ivaPorcentaje;
        return this;
    }
    
    public RenglonNotaCreditoBuilder withSubTotal(BigDecimal importe) {
        this.importe = importe;
        return this;
    }
    
    public RenglonNotaCreditoBuilder withSubTotalBruto(BigDecimal importeBruto) {
        this.importeBruto = importeBruto;
        return this;
    }
    
    public RenglonNotaCreditoBuilder withImporte(BigDecimal importeNeto) {
        this.importeNeto = importeNeto;
        return this;
    }
    
}
