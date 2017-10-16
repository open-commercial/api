package sic.builder;

import java.util.Date;
import sic.modelo.Empresa;
import sic.modelo.Medida;
import sic.modelo.Producto;
import sic.modelo.Proveedor;
import sic.modelo.Rubro;

public class ProductoBuilder {

    private Long id_Producto = 0L;
    private String codigo = "ABC123";
    private String descripcion = "Cinta adhesiva doble faz 3M";
    private double cantidad = 10;
    private double cantMinima = 2;    
    private double ventaMinima = 0;
    private Medida medida = new MedidaBuilder().build();
    private double precioCosto = 100;
    private double ganancia_porcentaje = 50;
    private double ganancia_neto = 50;
    private double precioVentaPublico = 150;
    private double iva_porcentaje = 21;
    private double iva_neto = 31.5;
    private double impuestoInterno_porcentaje = 0;
    private double impuestoInterno_neto = 0;
    private double precioLista = 181.5;    
    private Rubro rubro = new RubroBuilder().build();
    private boolean ilimitado = false;
    private Date fechaUltimaModificacion = new Date(1463540400000L); // 18-05-2016
    private String estanteria = "A";
    private String estante = "1";
    private Proveedor proveedor = new ProveedorBuilder().build();
    private String nota = "Cumple con las normas ISO";
    private Date fechaAlta = new Date(1458010800000L); // 15-03-2016;
    private Date fechaVencimiento = new Date(1597892400000L); // 20-08-2020
    private Empresa empresa = new EmpresaBuilder().build();
    private boolean eliminado = false;
    
    public Producto build() {
        return new Producto(id_Producto, codigo, descripcion, cantidad, cantMinima, ventaMinima, medida,
                precioCosto, ganancia_porcentaje, ganancia_neto, precioVentaPublico,
                iva_porcentaje, iva_neto, impuestoInterno_porcentaje, impuestoInterno_neto, precioLista,
                rubro, ilimitado, fechaUltimaModificacion, estanteria, estante, proveedor, nota,
                fechaAlta, fechaVencimiento, empresa, eliminado);
    }
    
    public ProductoBuilder withId_Producto(Long id_Producto) {
        this.id_Producto = id_Producto;
        return this;
    }
    
    public ProductoBuilder withCodigo(String codigo) {
        this.codigo = codigo;
        return this;
    }
    
    public ProductoBuilder withDescripcion(String descripcion) {
        this.descripcion = descripcion;
        return this;
    }
    
    public ProductoBuilder withCantidad(double cantidad) {
        this.cantidad = cantidad;
        return this;
    }
    
    public ProductoBuilder withCantMinima(double cantMinima) {
        this.cantMinima = cantMinima;
        return this;
    }
    
    public ProductoBuilder withVentaMinima(double ventaMinima) {
        this.ventaMinima = ventaMinima;
        return this;
    }
    
    public ProductoBuilder withMedida(Medida medida) {
        this.medida = medida;
        return this;
    }
    
    public ProductoBuilder withPrecioCosto(double precioCosto) {
        this.precioCosto = precioCosto;
        return this;
    }
    
    public ProductoBuilder withGanancia_porcentaje(double ganancia_porcentaje) {
        this.ganancia_porcentaje = ganancia_porcentaje;
        return this;
    }
    
    public ProductoBuilder withGanancia_neto(double ganancia_neto) {
        this.ganancia_neto = ganancia_neto;
        return this;
    }
    
    public ProductoBuilder withPrecioVentaPublico(double precioVentaPublico) {
        this.precioVentaPublico = precioVentaPublico;
        return this;
    }
    
    public ProductoBuilder withIva_porcentaje(double iva_porcentaje) {
        this.iva_porcentaje = iva_porcentaje;
        return this;
    }
    
    public ProductoBuilder withIva_neto(double iva_neto) {
        this.iva_neto = iva_neto;
        return this;
    }
    
    public ProductoBuilder withImpuestoInterno_porcentaje(double impuestoInterno_porcentaje) {
        this.impuestoInterno_porcentaje = impuestoInterno_porcentaje;
        return this;
    }
    
    public ProductoBuilder withImpuestoInterno_neto(double impuestoInterno_neto) {
        this.impuestoInterno_neto = impuestoInterno_neto;
        return this;
    }
    
    public ProductoBuilder withPrecioLista(double precioLista) {
        this.precioLista = precioLista;
        return this;
    }
    
    public ProductoBuilder withRubro(Rubro rubro) {
        this.rubro = rubro;
        return this;
    }
    
    public ProductoBuilder withIlimitado(boolean ilimitado) {
        this.ilimitado = ilimitado;
        return this;
    }
    
    public ProductoBuilder withFechaUltimaModificacion(Date fechaUltimaModificacion) {
        this.fechaUltimaModificacion = fechaUltimaModificacion;
        return this;
    }
    
    public ProductoBuilder withEstanteria(String estanteria) {
        this.estanteria = estanteria;
        return this;
    }
    
    public ProductoBuilder withEstante(String estante) {
        this.estante = estante;
        return this;
    }
    
    public ProductoBuilder withProveedor(Proveedor proveedor) {
        this.proveedor = proveedor;
        return this;
    }
    
    public ProductoBuilder withNota(String nota) {
        this.nota = nota;
        return this;
    }
    
    public ProductoBuilder withFechaAlta(Date fechaAlta) {
        this.fechaAlta = fechaAlta;
        return this;
    }
    
    public ProductoBuilder withFechaVencimiento(Date fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
        return this;
    }
    
    public ProductoBuilder withEmpresa(Empresa empresa) {
        this.empresa = empresa;
        return this;
    }
    
    public ProductoBuilder withEliminado(boolean eliminado) {
        this.eliminado = eliminado;
        return this;
    }
}
