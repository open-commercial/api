package sic.util;

import sic.dto.NuevoRenglonFacturaDTO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class CalculosComprobante {

  private CalculosComprobante() {
  }

  private static final BigDecimal CIEN = new BigDecimal("100");

  public static BigDecimal calcularImporte(BigDecimal cantidad, BigDecimal precioUnitario, BigDecimal descuentoNeto) {
    return (precioUnitario.subtract(descuentoNeto)).multiply(cantidad);
  }

  public static BigDecimal calcularSubTotal(BigDecimal[] importes) {
    BigDecimal resultado = BigDecimal.ZERO;
    for (BigDecimal importe : importes) {
      resultado = resultado.add(importe);
    }
    return resultado;
  }

  public static BigDecimal calcularProporcion(BigDecimal monto, BigDecimal porcentaje) {
    BigDecimal resultado = BigDecimal.ZERO;
    if (porcentaje != null && porcentaje.compareTo(BigDecimal.ZERO) != 0) {
      resultado = monto.multiply(porcentaje).divide(CIEN, 15, RoundingMode.HALF_UP);
    }
    return resultado;
  }

  public static BigDecimal calcularSubTotalBruto(
    boolean quitarIVA,
    BigDecimal subTotal,
    BigDecimal recargoNeto,
    BigDecimal descuentoNeto,
    BigDecimal iva105Neto,
    BigDecimal iva21Neto) {
    BigDecimal resultado = subTotal.add(recargoNeto != null ? recargoNeto : BigDecimal.ZERO).subtract(descuentoNeto != null ? descuentoNeto : BigDecimal.ZERO);
    if (quitarIVA) {
      resultado = resultado.subtract(iva105Neto.add(iva21Neto));
    }
    return resultado;
  }

  public static BigDecimal calcularTotal(
    BigDecimal subTotalBruto, BigDecimal iva105Neto, BigDecimal iva21Neto) {
    return subTotalBruto.add(iva105Neto).add(iva21Neto);
  }

  public static long[] getArrayDeIdProductoParaFactura(List<NuevoRenglonFacturaDTO> nuevosRenglones) {
    long[] idProductoItem = new long[nuevosRenglones.size()];
    for (int i = 0; i < nuevosRenglones.size(); ++i) {
      idProductoItem[i] = nuevosRenglones.get(i).getIdProducto();
    }
    return idProductoItem;
  }

  public static BigDecimal[] getArrayDeCantidadesProductoParaFactura(List<NuevoRenglonFacturaDTO> nuevosRenglones) {
    BigDecimal[] cantidades = new BigDecimal[nuevosRenglones.size()];
    for (int i = 0; i < nuevosRenglones.size(); ++i) {
      cantidades[i] = nuevosRenglones.get(i).getCantidad();
    }
    return cantidades;
  }

  public static BigDecimal[] getArrayDeBonificacionesParaFactura(List<NuevoRenglonFacturaDTO> nuevosRenglones) {
    BigDecimal[] bonificaciones = new BigDecimal[nuevosRenglones.size()];
    for (int i = 0; i < nuevosRenglones.size(); ++i) {
      bonificaciones[i] = nuevosRenglones.get(i).getBonificacion();
    }
    return bonificaciones;
  }
}
