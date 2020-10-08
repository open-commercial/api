package sic.model;

import lombok.*;
import sic.modelo.TipoDeComprobante;
import sic.modelo.RenglonFactura;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(
    callSuper = true,
    exclude = {"razonSocialProveedor", "idProveedor"})
@Builder
public class FacturaCompra extends Factura {

  private Long idProveedor;
  private String razonSocialProveedor;
  private LocalDateTime fechaAlta;

  @Builder(builderMethodName = "facturaCompraBuilder")
  public FacturaCompra(
      long idFactura,
      LocalDateTime fecha,
      LocalDateTime fechaAlta,
      TipoDeComprobante tipoDeComprobante,
      long numSerie,
      long numFactura,
      LocalDate fechaVencimiento,
      Long nroPedido,
      Long idTransportista,
      String nombreTransportista,
      List<RenglonFactura> renglones,
      BigDecimal subTotal,
      BigDecimal recargoPorcentaje,
      BigDecimal recargoNeto,
      BigDecimal descuentoPorcentaje,
      BigDecimal descuentoNeto,
      BigDecimal subTotalBruto,
      BigDecimal iva105Neto,
      BigDecimal iva21Neto,
      BigDecimal impuestoInternoNeto,
      BigDecimal total,
      String observaciones,
      BigDecimal cantidadArticulos,
      long idSucursal,
      String nombreSucursal,
      Long idUsuario,
      String nombreUsuario,
      boolean eliminada,
      long cae,
      LocalDate vencimientoCae,
      Long idProveedor,
      String razonSocialProveedor) {
    super(
        idFactura,
        fecha,
        tipoDeComprobante,
        numSerie,
        numFactura,
        fechaVencimiento,
        nroPedido,
        idTransportista,
        nombreTransportista,
        renglones,
        subTotal,
        recargoPorcentaje,
        recargoNeto,
        descuentoPorcentaje,
        descuentoNeto,
        subTotalBruto,
        iva105Neto,
        iva21Neto,
        impuestoInternoNeto,
        total,
        observaciones,
        cantidadArticulos,
        idSucursal,
        nombreSucursal,
        idUsuario,
        nombreUsuario,
        eliminada,
        cae,
        vencimientoCae);
    this.idProveedor = idProveedor;
    this.razonSocialProveedor = razonSocialProveedor;
    this.fechaAlta = fechaAlta;
  }
}
