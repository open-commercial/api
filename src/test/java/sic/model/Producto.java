package sic.model;

import lombok.*;
import org.apache.commons.lang3.RandomStringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"codigo", "descripcion", "eliminado"})
@Builder
public class Producto {

    private Long idProducto;
    private String codigo;
    private String descripcion;
    private Set<CantidadEnSucursal> cantidadEnSucursales;
    private BigDecimal cantidadTotalEnSucursales;
    private boolean hayStock;
    private BigDecimal cantMinima;
    private BigDecimal bulto;
    private String nombreMedida ;
    private BigDecimal precioCosto;
    private BigDecimal gananciaPorcentaje;
    private BigDecimal gananciaNeto;
    private BigDecimal precioVentaPublico;
    private BigDecimal ivaPorcentaje;
    private BigDecimal ivaNeto;
    private BigDecimal precioLista;
    private String nombreRubro;
    private boolean ilimitado;
    private boolean publico;
    private boolean oferta;
    private BigDecimal porcentajeBonificacionOferta;
    private BigDecimal porcentajeBonificacionPrecio;
    private BigDecimal precioBonificado;
    private LocalDateTime fechaUltimaModificacion;
    private String estanteria;
    private String estante;
    private String razonSocialProveedor;
    private String nota;
    private LocalDateTime fechaAlta;
    private LocalDate fechaVencimiento;
    private boolean eliminado = false;
    private String urlImagen;
    private byte[] imagen;
}
