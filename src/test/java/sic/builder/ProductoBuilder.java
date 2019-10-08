package sic.builder;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import sic.modelo.dto.CantidadEnSucursalDTO;
import sic.modelo.dto.ProductoDTO;

public class ProductoBuilder {

    private Long idProducto = 0L;
    private String codigo = "ABC123";
    private String descripcion = "Cinta adhesiva doble faz 3M";
    private List<CantidadEnSucursalDTO> cantidadEnSucursales;
    private BigDecimal cantidadTotalEnSucursales;
    private boolean hayStock = true;
    private BigDecimal cantMinima = new BigDecimal("2");    
    private BigDecimal bulto = BigDecimal.ONE;
    private String nombreMedida = "Unidad";
    private BigDecimal precioCosto = new BigDecimal("100");
    private BigDecimal ganancia_porcentaje = new BigDecimal("50");
    private BigDecimal ganancia_neto = new BigDecimal("50");
    private BigDecimal precioVentaPublico = new BigDecimal("150");
    private BigDecimal iva_porcentaje = new BigDecimal("21");
    private BigDecimal iva_neto = new BigDecimal("31.5");
    private BigDecimal precioLista = new BigDecimal("181.5");
    private String nombreRubro = "Ferreteria";
    private boolean ilimitado = false;
    private boolean publico = true;
    private boolean oferta = false;
    private BigDecimal porcentajeBonificacionOferta = BigDecimal.TEN;
    private BigDecimal precioListaBonificado = new BigDecimal("18.15");
    private Date fechaUltimaModificacion = new Date(1463540400000L); // 18-05-2016
    private String estanteria = "A";
    private String estante = "1";
    private String razonSocialProveedor = "Abrasol";
    private String nota = "Cumple con las normas ISO";
    private Date fechaAlta = new Date(1458010800000L); // 15-03-2016;
    private Date fechaVencimiento = new Date(1597892400000L); // 20-08-2020
    private boolean eliminado = false;
    private String urlImagen;

  public ProductoDTO build() {
    return new ProductoDTO(
        idProducto,
        codigo,
        descripcion,
        cantidadEnSucursales,
      cantidadTotalEnSucursales,
        hayStock,
        cantMinima,
        bulto,
        nombreMedida,
        precioCosto,
        ganancia_porcentaje,
        ganancia_neto,
        precioVentaPublico,
        iva_porcentaje,
        iva_neto,
        precioLista,
        nombreRubro,
        ilimitado,
        publico,
        oferta,
      porcentajeBonificacionOferta,
      precioListaBonificado,
        fechaUltimaModificacion,
        estanteria,
        estante,
        razonSocialProveedor,
        nota,
        fechaAlta,
        fechaVencimiento,
        eliminado,
        urlImagen);
  }

  public ProductoBuilder withId_Producto(Long idProducto) {
    this.idProducto = idProducto;
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
    
    public ProductoBuilder withCantidadSucursales(List<CantidadEnSucursalDTO> cantidadEnSucursales) {
        this.cantidadEnSucursales = cantidadEnSucursales;
        return this;
    }

  public ProductoBuilder withCantidadEnSucursales(BigDecimal cantidad) {
    this.cantidadTotalEnSucursales = cantidad;
    return this;
  }

    public ProductoBuilder whitHayStock(boolean hayStock) {
        this.hayStock = hayStock;
        return this;
    }
    
    public ProductoBuilder withCantMinima(BigDecimal cantMinima) {
        this.cantMinima = cantMinima;
        return this;
    }
    
    public ProductoBuilder withBulto(BigDecimal bulto) {
        this.bulto = bulto;
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

    public ProductoBuilder withPublico(boolean publico) {
        this.publico = publico;
        return this;
    }


    public ProductoBuilder withOferta(boolean oferta) {
        this.oferta = oferta;
        return this;
    }

  public ProductoBuilder withBonificacionOferta(BigDecimal porcentajeBonificacionOferta) {
    this.porcentajeBonificacionOferta = porcentajeBonificacionOferta;
    return this;
  }

  public ProductoBuilder withPrecioListaBonificado(BigDecimal precioListaBonificado) {
    this.precioListaBonificado = precioListaBonificado;
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
    
    public ProductoBuilder withEliminado(boolean eliminado) {
        this.eliminado = eliminado;
        return this;
    }
    public ProductoBuilder withUrlImagen(String urlImagen) {
        this.urlImagen = urlImagen;
        return this;
    }
}
