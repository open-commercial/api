package sic.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import sic.modelo.RenglonFactura;
import sic.modelo.TipoDeComprobante;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(
    exclude = {
      "idFactura",
      "fecha",
      "numSerie",
      "numFactura",
      "nombreTransportista",
      "renglones",
      "nombreSucursal",
      "idUsuario",
      "nombreUsuario",
      "cantidadArticulos"
    })
@JsonIdentityInfo(
    generator = ObjectIdGenerators.PropertyGenerator.class,
    property = "idFactura",
    scope = Factura.class)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = FacturaCompra.class, name = "FacturaCompra"),
  @JsonSubTypes.Type(value = FacturaVenta.class, name = "FacturaVenta"),
})
public abstract class Factura {

  private long idFactura;
  private LocalDateTime fecha;
  private TipoDeComprobante tipoComprobante;
  private long numSerie;
  private long numFactura;
  private LocalDate fechaVencimiento;
  private Long idPedido;
  private Long nroPedido;
  private Long idTransportista;
  private String nombreTransportista;
  private List<RenglonFactura> renglones;
  private BigDecimal subTotal;
  private BigDecimal recargoPorcentaje;
  private BigDecimal recargoNeto;
  private BigDecimal descuentoPorcentaje;
  private BigDecimal descuentoNeto;
  private BigDecimal subTotalBruto;
  private BigDecimal iva105Neto;
  private BigDecimal iva21Neto;
  private BigDecimal impuestoInternoNeto;
  private BigDecimal total;
  private String observaciones;
  private BigDecimal cantidadArticulos;
  private long idSucursal;
  private String nombreSucursal;
  private Long idUsuario;
  private String nombreUsuario;
  private boolean eliminada;
  private long cae;
  private LocalDate vencimientoCae;
}
