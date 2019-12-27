package sic.modelo.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Set;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"codigo", "descripcion", "eliminado"})
@Builder
public class ProductoDTO implements Serializable {

  private Long idProducto;
  private String codigo;
  private String descripcion;
  private Set<CantidadEnSucursalDTO> cantidadEnSucursales;
  private BigDecimal cantidadTotalEnSucursales;
  private boolean hayStock;
  private BigDecimal cantMinima;
  private BigDecimal bulto;
  private String nombreMedida;
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
  private boolean eliminado;
  private String urlImagen;
}
