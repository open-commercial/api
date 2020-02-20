package sic.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RenglonPedido implements Serializable {

  private long idRenglonPedido;
  private long idProductoItem;
  private String codigoItem;
  private String descripcionItem;
  private String medidaItem;
  private String urlImagenItem;
  private boolean oferta;
  private BigDecimal precioUnitario;
  private BigDecimal cantidad;
  private BigDecimal bonificacionPorcentaje;
  private BigDecimal bonificacionNeta;
  private BigDecimal importeAnterior;
  private BigDecimal importe;

}
