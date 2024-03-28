package sic.modelo;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.DecimalMin;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import sic.config.Views;

@Entity
@Table(name = "notacredito")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@JsonView(Views.Comprador.class)
public class NotaCredito extends Nota implements Serializable {

  @Column(nullable = false)
  private boolean modificaStock;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "idNota")
  @JsonProperty(access = JsonProperty.Access.READ_WRITE)
  @Column(nullable = false)
  private List<RenglonNotaCredito> renglonesNotaCredito;

  @Column(precision = 25, scale = 15)
  @DecimalMin(value = "0", message = "{mensaje_subtotal_negativo}")
  private BigDecimal subTotal;

  @Column(precision = 25, scale = 15)
  @DecimalMin(value = "0", message = "{mensaje_recargo_porcentaje_negativo}")
  private BigDecimal recargoPorcentaje;

  @Column(precision = 25, scale = 15)
  @DecimalMin(value = "0", message = "{mensaje_recargo_neto_negativo}")
  private BigDecimal recargoNeto;

  @Column(precision = 25, scale = 15)
  @DecimalMin(value = "0", message = "{mensaje_descuento_porcentaje_negativo}")
  private BigDecimal descuentoPorcentaje;

  @Column(precision = 25, scale = 15)
  @DecimalMin(value = "0", message = "{mensaje_descuento_neto_negativo}")
  private BigDecimal descuentoNeto;

  public NotaCredito() {}

  public NotaCredito(
      long idNota,
      long serie,
      long nroNota,
      boolean eliminada,
      TipoDeComprobante tipoDeComprobante,
      LocalDateTime fecha,
      Sucursal sucursal,
      Usuario usuario,
      Cliente cliente,
      FacturaVenta facturaVenta,
      Movimiento movimiento,
      String motivo,
      List<RenglonNotaCredito> renglones,
      BigDecimal subTotalBruto,
      BigDecimal iva21Neto,
      BigDecimal iva105Neto,
      BigDecimal total,
      long cae,
      LocalDate vencimientoCAE,
      long numSerieAfip,
      long numFacturaAfip) {

    super(
        idNota,
        serie,
        nroNota,
        eliminada,
        tipoDeComprobante,
        fecha,
      sucursal,
        usuario,
        cliente,
        facturaVenta,
        null,
        null,
        movimiento,
        motivo,
        subTotalBruto,
        iva21Neto,
        iva105Neto,
        total,
        cae,
        vencimientoCAE,
        numSerieAfip,
        numFacturaAfip);
    this.renglonesNotaCredito = renglones;
  }

  public NotaCredito(
      long idNota,
      long serie,
      long nroNota,
      boolean eliminada,
      TipoDeComprobante tipoDeComprobante,
      LocalDateTime fecha,
      Sucursal sucursal,
      Usuario usuario,
      Proveedor proveedor,
      FacturaCompra facturaCompra,
      Movimiento movimiento,
      String motivo,
      List<RenglonNotaCredito> renglones,
      BigDecimal subTotalBruto,
      BigDecimal iva21Neto,
      BigDecimal iva105Neto,
      BigDecimal total,
      long cae,
      LocalDate vencimientoCAE,
      long numSerieAfip,
      long numFacturaAfip) {

    super(
        idNota,
        serie,
        nroNota,
        eliminada,
        tipoDeComprobante,
        fecha,
      sucursal,
        usuario,
        null,
        null,
        proveedor,
        facturaCompra,
        movimiento,
        motivo,
        subTotalBruto,
        iva21Neto,
        iva105Neto,
        total,
        cae,
        vencimientoCAE,
        numSerieAfip,
        numFacturaAfip);
    this.renglonesNotaCredito = renglones;
  }
}
