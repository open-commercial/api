package sic.builder;

import java.math.BigDecimal;
import java.util.Set;

import sic.modelo.*;
import sic.modelo.dto.CantidadEnSucursalDTO;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;

public class ProductoBuilder {

    private Long idProducto = 0L;
    private String codigo = "ABC123";
    private String descripcion = "Cinta adhesiva doble faz 3M";
    private Set<CantidadEnSucursal> cantidadEnSucursales;
    private BigDecimal cantidadTotalEnSucursales;
    private boolean hayStock = true;
    private BigDecimal cantMinima = new BigDecimal("2");
    private BigDecimal bulto = BigDecimal.ONE;
    private Medida medida;
    private BigDecimal precioCosto = new BigDecimal("100");
    private BigDecimal gananciaPorcentaje = new BigDecimal("50");
    private BigDecimal gananciaNeto = new BigDecimal("50");
    private BigDecimal precioVentaPublico = new BigDecimal("150");
    private BigDecimal ivaPorcentaje = new BigDecimal("21");
    private BigDecimal ivaNeto = new BigDecimal("31.5");
    private BigDecimal precioLista = new BigDecimal("181.5");
    private Rubro rubro;
    private boolean ilimitado = false;
    private boolean publico = true;
    private boolean oferta = false;
    private BigDecimal porcentajeBonificacionOferta = BigDecimal.TEN;
    private BigDecimal porcentajeBonificacionPrecio = BigDecimal.TEN;
    private BigDecimal precioBonificado = new BigDecimal("18.15");
    private LocalDateTime fechaUltimaModificacion =
            LocalDateTime.of(2016, Month.MAY, 18, 0, 0); // 18-05-2016
    private Proveedor proveedor;
    private String nota = "Cumple con las normas ISO";
    private LocalDateTime fechaAlta = LocalDateTime.of(2016, Month.MARCH, 15, 00, 00);; // 15-03-2016;
    private LocalDate fechaVencimiento = LocalDate.of(2020, Month.OCTOBER, 20);; // 20-08-2020
    private boolean eliminado = false;
    private String urlImagen;
    private Long version;

    public Producto build() {
    return new Producto(
        idProducto,
        codigo,
        descripcion,
        cantidadEnSucursales,
        cantidadTotalEnSucursales,
        cantMinima,
        hayStock,
        bulto,
        medida,
        precioCosto,
        gananciaPorcentaje,
        gananciaNeto,
        precioVentaPublico,
        ivaPorcentaje,
        ivaNeto,
        precioLista,
        oferta,
        porcentajeBonificacionOferta,
        porcentajeBonificacionPrecio,
        precioBonificado,
        rubro,
        ilimitado,
        publico,
        fechaUltimaModificacion,
        proveedor,
        nota,
        fechaAlta,
        fechaVencimiento,
        eliminado,
        urlImagen,
        version);
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

    public ProductoBuilder withCantidadSucursales(Set<CantidadEnSucursal> cantidadEnSucursales) {
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

    public ProductoBuilder withNombreMedida(Medida medida) {
        this.medida = medida;
        return this;
    }

    public ProductoBuilder withPrecioCosto(BigDecimal precioCosto) {
        this.precioCosto = precioCosto;
        return this;
    }

    public ProductoBuilder withGananciaPorcentaje(BigDecimal gananciaPorcentaje) {
        this.gananciaPorcentaje = gananciaPorcentaje;
        return this;
    }

    public ProductoBuilder withGananciaNeto(BigDecimal gananciaNeto) {
        this.gananciaNeto = gananciaNeto;
        return this;
    }

    public ProductoBuilder withPrecioVentaPublico(BigDecimal precioVentaPublico) {
        this.precioVentaPublico = precioVentaPublico;
        return this;
    }

    public ProductoBuilder withIvaPorcentaje(BigDecimal ivaPorcentaje) {
        this.ivaPorcentaje = ivaPorcentaje;
        return this;
    }

    public ProductoBuilder withIvaNeto(BigDecimal ivaNeto) {
        this.ivaNeto = ivaNeto;
        return this;
    }

    public ProductoBuilder withPrecioLista(BigDecimal precioLista) {
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

    public ProductoBuilder withPorcentajeBonificacionPrecio(BigDecimal porcentajeBonificacionPrecio) {
        this.porcentajeBonificacionPrecio = porcentajeBonificacionPrecio;
        return this;
    }

    public ProductoBuilder withPrecioBonificado(BigDecimal precioBonificado) {
        this.precioBonificado = precioBonificado;
        return this;
    }

    public ProductoBuilder withFechaUltimaModificacion(LocalDateTime fechaUltimaModificacion) {
        this.fechaUltimaModificacion = fechaUltimaModificacion;
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

    public ProductoBuilder withFechaAlta(LocalDateTime fechaAlta) {
        this.fechaAlta = fechaAlta;
        return this;
    }

    public ProductoBuilder withFechaVencimiento(LocalDate fechaVencimiento) {
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
