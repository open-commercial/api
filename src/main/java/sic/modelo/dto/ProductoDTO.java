package sic.modelo.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductoDTO implements Serializable {

  private Long id_Producto = 0L;
  private String codigo = "ABC123";
  private String descripcion = "Cinta adhesiva doble faz 3M";
  private BigDecimal cantidad = BigDecimal.TEN;
  private BigDecimal cantMinima = new BigDecimal("2");
  private BigDecimal ventaMinima = BigDecimal.ZERO;
  private String nombreMedida = "Unidad";
  private BigDecimal precioCosto = new BigDecimal("100");
  private BigDecimal gananciaPorcentaje = new BigDecimal("50");
  private BigDecimal gananciaNeto = new BigDecimal("50");
  private BigDecimal precioVentaPublico = new BigDecimal("150");
  private BigDecimal ivaPorcentaje = new BigDecimal("21");
  private BigDecimal ivaNeto = new BigDecimal("31.5");
  private BigDecimal impuestoInternoPorcentaje = BigDecimal.ZERO;
  private BigDecimal impuestoInternoNeto = BigDecimal.ZERO;
  private BigDecimal precioLista = new BigDecimal("181.5");
  private String nombreRubro = "Ferreteria";
  private boolean ilimitado = false;
  private Date fechaUltimaModificacion = new Date(1463540400000L); // 18-05-2016
  private String estanteria = "A";
  private String estante = "1";
  private String razonSocialProveedor = "Abrasol";
  private String nota = "Cumple con las normas ISO9001";
  private Date fechaAlta = new Date(1458010800000L); // 15-03-2016;
  private Date fechaVencimiento = new Date(1597892400000L); // 20-08-2020
  private String nombreEmpresa = "Globo De Oro";
  private boolean eliminado = false;
}
