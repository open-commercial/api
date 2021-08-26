package sic.model;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
  private Set<CantidadEnSucursal> cantidadEnSucursalesDisponible;
  private BigDecimal cantidadReservada;
  private BigDecimal cantidadTotalEnSucursales;
  private BigDecimal cantidadTotalEnSucursalesDisponible;
  private boolean hayStock;
  private BigDecimal cantMinima;
  private Long idMedida;
  private String nombreMedida;
  private BigDecimal precioCosto;
  private BigDecimal gananciaPorcentaje;
  private BigDecimal gananciaNeto;
  private BigDecimal precioVentaPublico;
  private BigDecimal ivaPorcentaje;
  private BigDecimal ivaNeto;
  private BigDecimal precioLista;
  private Long idRubro;
  private String nombreRubro;
  private boolean ilimitado;
  private boolean publico;
  private boolean oferta;
  private BigDecimal porcentajeBonificacionOferta;
  private BigDecimal porcentajeBonificacionPrecio;
  private BigDecimal precioBonificado;
  private LocalDateTime fechaUltimaModificacion;
  private Long idProveedor;
  private String razonSocialProveedor;
  private String nota;
  private LocalDateTime fechaAlta;
  private LocalDate fechaVencimiento;
  private boolean eliminado;
  private String urlImagen;
  private byte[] imagen;
  private Long version;
}
