package sic.builder;

import sic.modelo.RenglonNotaCredito;

public class RenglonNotaCreditoBuilder {
    
    private long id_RenglonNota = 1L;
    private long id_ProductoItem = 1L;
    private String codigoItem = "1";
    private String descripcionItem = "Acero Valyrio";
    private String medidaItem = "Kilos";
    private double descuentoPorcentaje = 10;
    private double descuentoNeto = 15.8;
    private double cantidad = 2;  
    private double precioUnitario = 100;
    private double ivaPorcentaje =  21;
    private double ivaNeto;
    private double importe  = 158; //sin nada
    private double importeBruto = 142.2;  //con descuentos y recargos, sin iva
    private double importeNeto = 172.062; //con descuentos, recargos y con iva
    
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
    
    public RenglonNotaCreditoBuilder withDescuentoPorcentaje(double descuentoPorcentaje) {
        this.descuentoPorcentaje = descuentoPorcentaje;
        return this;
    }
    
    public RenglonNotaCreditoBuilder withDescuentoNeto(double descuentoNeto) {
        this.descuentoNeto = descuentoNeto;
        return this;
    }
    
    public RenglonNotaCreditoBuilder withCantidad(double cantidad) {
        this.cantidad = cantidad;
        return this;
    }
    
    public RenglonNotaCreditoBuilder withPrecioUnitario(double precioUnitario) {
        this.precioUnitario = precioUnitario;
        return this;
    }
    
    public RenglonNotaCreditoBuilder withIvaNeto(double ivaNeto) {
        this.ivaNeto = ivaNeto;
        return this;
    }
    
    public RenglonNotaCreditoBuilder withIvaPorcentaje(double ivaPorcentaje) {
        this.ivaPorcentaje = ivaPorcentaje;
        return this;
    }
    
    public RenglonNotaCreditoBuilder withSubTotal(double importe) {
        this.importe = importe;
        return this;
    }
    
    public RenglonNotaCreditoBuilder withSubTotalBruto(double importeBruto) {
        this.importeBruto = importeBruto;
        return this;
    }
    
    public RenglonNotaCreditoBuilder withImporte(double importeNeto) {
        this.importeNeto = importeNeto;
        return this;
    }
    
}
