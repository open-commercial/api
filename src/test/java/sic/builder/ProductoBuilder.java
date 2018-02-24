package sic.builder;

import java.math.BigDecimal;
import java.util.Date;
import sic.modelo.dto.ProductoDTO;

public class ProductoBuilder {

    private Long id_Producto = 0L;
    private String codigo = "ABC123";
    private String descripcion = "Cinta adhesiva doble faz 3M";
    private BigDecimal cantidad = BigDecimal.TEN;
    private BigDecimal cantMinima = new BigDecimal("2");    
    private BigDecimal ventaMinima = BigDecimal.ZERO;
    private String nombreMedida = "Unidad";
    private BigDecimal precioCosto = new BigDecimal("100");
    private BigDecimal ganancia_porcentaje = new BigDecimal("50");
    private BigDecimal ganancia_neto = new BigDecimal("50");
    private BigDecimal precioVentaPublico = new BigDecimal("150");
    private BigDecimal iva_porcentaje = new BigDecimal("21");
    private BigDecimal iva_neto = new BigDecimal("31.5");
    private BigDecimal impuestoInterno_porcentaje = BigDecimal.ZERO;
    private BigDecimal impuestoInterno_neto = BigDecimal.ZERO;
    private BigDecimal precioLista = new BigDecimal("181.5");    
    private String nombreRubro = "Ferreteria";
    private boolean ilimitado = false;
    private Date fechaUltimaModificacion = new Date(1463540400000L); // 18-05-2016
    private String estanteria = "A";
    private String estante = "1";
    private String razonSocialProveedor = "Abrasol";
    private String nota = "Cumple con las normas ISO";
    private Date fechaAlta = new Date(1458010800000L); // 15-03-2016;
    private Date fechaVencimiento = new Date(1597892400000L); // 20-08-2020
    private String nombreEmpresa = "Globo De Oro";
    private boolean eliminado = false;
    
    public ProductoDTO build() {
        return new ProductoDTO(id_Producto, codigo, descripcion, cantidad, cantMinima, ventaMinima, nombreMedida,
                precioCosto, ganancia_porcentaje, ganancia_neto, precioVentaPublico,
                iva_porcentaje, iva_neto, impuestoInterno_porcentaje, impuestoInterno_neto, precioLista,
                nombreRubro, ilimitado, fechaUltimaModificacion, estanteria, estante, razonSocialProveedor, nota,
                fechaAlta, fechaVencimiento, nombreEmpresa, eliminado);
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
    
    public ProductoBuilder withCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
        return this;
    }
    
    public ProductoBuilder withCantMinima(BigDecimal cantMinima) {
        this.cantMinima = cantMinima;
        return this;
    }
    
    public ProductoBuilder withVentaMinima(BigDecimal ventaMinima) {
        this.ventaMinima = ventaMinima;
        return this;
    }
    
    public ProductoBuilder withNombreMedida(String nombreMedida) {
        this.nombreMedida = nombreMedida;
        return this;
    }
    
    public ProductoBuilder withPrecioCosto(BigDecimal precioCosto) {
        this.precioCosto = precioCosto;
        return this;
    }
    
    public ProductoBuilder withGanancia_porcentaje(BigDecimal ganancia_porcentaje) {
        this.ganancia_porcentaje = ganancia_porcentaje;
        return this;
    }
    
    public ProductoBuilder withGanancia_neto(BigDecimal ganancia_neto) {
        this.ganancia_neto = ganancia_neto;
        return this;
    }
    
    public ProductoBuilder withPrecioVentaPublico(BigDecimal precioVentaPublico) {
        this.precioVentaPublico = precioVentaPublico;
        return this;
    }
    
    public ProductoBuilder withIva_porcentaje(BigDecimal iva_porcentaje) {
        this.iva_porcentaje = iva_porcentaje;
        return this;
    }
    
    public ProductoBuilder withIva_neto(BigDecimal iva_neto) {
        this.iva_neto = iva_neto;
        return this;
    }
    
    public ProductoBuilder withImpuestoInterno_porcentaje(BigDecimal impuestoInterno_porcentaje) {
        this.impuestoInterno_porcentaje = impuestoInterno_porcentaje;
        return this;
    }
    
    public ProductoBuilder withImpuestoInterno_neto(BigDecimal impuestoInterno_neto) {
        this.impuestoInterno_neto = impuestoInterno_neto;
        return this;
    }
    
    public ProductoBuilder withPrecioLista(BigDecimal precioLista) {
        this.precioLista = precioLista;
        return this;
    }
    
    public ProductoBuilder withNombreRubro(String nombreRubro) {
        this.nombreRubro = nombreRubro;
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
    
    public ProductoBuilder withRazonSocialProveedor(String razonSocialProveedor) {
        this.razonSocialProveedor = razonSocialProveedor;
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
    
    public ProductoBuilder withNombreEmpresa(String nombreEmpresa) {
        this.nombreEmpresa = nombreEmpresa;
        return this;
    }
    
    public ProductoBuilder withEliminado(boolean eliminado) {
        this.eliminado = eliminado;
        return this;
    }
}
