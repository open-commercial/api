package sic.modelo.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

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
  private BigDecimal cantidad;
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
  private boolean destacado;
  private Date fechaUltimaModificacion;
  private String estanteria;
  private String estante;
  private String razonSocialProveedor;
  private String nota;
  private Date fechaAlta;
  private Date fechaVencimiento;
  private boolean eliminado;
  private String urlImagen;
}
