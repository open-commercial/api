package org.opencommercial.integration.model;

import lombok.*;
import org.opencommercial.model.CategoriaIVA;
import org.opencommercial.model.TipoDeComprobante;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(
    callSuper = true,
    exclude = {
      "idCliente",
      "nombreFiscalCliente",
      "nroDeCliente",
      "categoriaIVACliente",
      "idViajanteCliente",
      "nombreViajanteCliente",
      "ubicacionCliente"
    })
@Builder
public class FacturaVentaTest extends FacturaTest {

  private Long idCliente;
  private String nombreFiscalCliente;
  private String nroDeCliente;
  private CategoriaIVA categoriaIVACliente;
  private Long idViajanteCliente;
  private String nombreViajanteCliente;
  private String ubicacionCliente;
  private RemitoTest remito;

  @Builder(builderMethodName = "facturaVentaBuilder")
  public FacturaVentaTest(
      long idFactura,
      LocalDateTime fecha,
      TipoDeComprobante tipoDeComprobante,
      long numSerie,
      long numFactura,
      LocalDate fechaVencimiento,
      Long idPedido,
      Long nroPedido,
      Long idTransportista,
      String nombreTransportista,
      List<RenglonFacturaTest> renglones,
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
      Long idCliente,
      String nombreFiscalCliente,
      String nroDeCliente,
      CategoriaIVA categoriaIVACliente,
      Long idViajanteCliente,
      String nombreViajanteCliente,
      String ubicacionCliente) {
    super(
        idFactura,
        fecha,
        tipoDeComprobante,
        numSerie,
        numFactura,
        fechaVencimiento,
        idPedido,
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
    this.idCliente = idCliente;
    this.nombreFiscalCliente = nombreFiscalCliente;
    this.nroDeCliente = nroDeCliente;
    this.categoriaIVACliente = categoriaIVACliente;
    this.idViajanteCliente = idViajanteCliente;
    this.nombreViajanteCliente = nombreViajanteCliente;
    this.ubicacionCliente = ubicacionCliente;
  }
}
