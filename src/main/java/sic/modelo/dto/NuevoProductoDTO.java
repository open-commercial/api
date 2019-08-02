package sic.modelo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NuevoProductoDTO {

  private String codigo;
  private String descripcion;
  private BigDecimal cantidad;

  private boolean hayStock;
  private BigDecimal precioBonificado;
  private BigDecimal cantMinima;
  private BigDecimal bulto;
  private BigDecimal precioCosto;
  private BigDecimal gananciaPorcentaje;
  private BigDecimal gananciaNeto;
  private BigDecimal precioVentaPublico;
  private BigDecimal ivaPorcentaje;
  private BigDecimal ivaNeto;
  private BigDecimal precioLista;
  private boolean ilimitado;
  private boolean publico;
  private boolean destacado;
  private Date fechaUltimaModificacion;
  private String estanteria;
  private String estante;
  private String nota;
  private Date fechaVencimiento;
  private boolean eliminado;
}
