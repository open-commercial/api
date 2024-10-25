package sic.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import sic.modelo.Resultados;
import sic.modelo.Sucursal;
import sic.modelo.TipoDeComprobante;
import sic.modelo.dto.NuevosResultadosComprobanteDTO;
import sic.service.FacturaServiceImpl;
import sic.service.SucursalServiceImpl;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class FacturaControllerTest {

  @Mock SucursalServiceImpl sucursalService;
  @Mock FacturaServiceImpl facturaService;

  @InjectMocks FacturaController facturaController;

  @Test
  void shouldGetTiposFacturaSegunSucursal() {
    Sucursal sucursal = new Sucursal();
    sucursal.setIdSucursal(3L);
    when(sucursalService.getSucursalPorId(3L)).thenReturn(sucursal);
    TipoDeComprobante[] tipoDeComprobantes =
        new TipoDeComprobante[] {TipoDeComprobante.FACTURA_A, TipoDeComprobante.FACTURA_B};
    when(facturaService.getTiposDeComprobanteSegunSucursal(sucursal))
        .thenReturn(tipoDeComprobantes);
    assertEquals(tipoDeComprobantes, facturaController.getTiposFacturaSegunSucursal(3L));
  }

  @Test
  void shouldCalcularResultadosFactura() {
    Resultados resultados = new Resultados();
    resultados.setSubTotal(new BigDecimal("11"));
    resultados.setTotal(new BigDecimal("11"));
    resultados.setIva21Neto(BigDecimal.ONE);
    resultados.setIva105Neto(BigDecimal.ZERO);
    resultados.setSubTotalBruto(new BigDecimal("11"));
    resultados.setDescuentoPorcentaje(BigDecimal.ONE);
    resultados.setDescuentoNeto(BigDecimal.TEN);
    resultados.setRecargoPorcentaje(BigDecimal.ONE);
    resultados.setRecargoNeto(BigDecimal.TEN);
    NuevosResultadosComprobanteDTO nuevosResultadosComprobanteDTO =
        NuevosResultadosComprobanteDTO.builder()
            .importe(new BigDecimal[] {BigDecimal.TEN, BigDecimal.ONE})
            .tipoDeComprobante(TipoDeComprobante.FACTURA_B)
            .build();
    when(facturaService.calcularResultadosFactura(nuevosResultadosComprobanteDTO))
        .thenReturn(resultados);
    assertEquals(
        resultados, facturaController.calcularResultadosFactura(nuevosResultadosComprobanteDTO));
    nuevosResultadosComprobanteDTO =
        NuevosResultadosComprobanteDTO.builder()
            .importe(new BigDecimal[] {BigDecimal.TEN, BigDecimal.ONE})
            .tipoDeComprobante(TipoDeComprobante.FACTURA_C)
            .build();
    when(facturaService.calcularResultadosFactura(nuevosResultadosComprobanteDTO))
        .thenReturn(resultados);
    assertEquals(
        resultados, facturaController.calcularResultadosFactura(nuevosResultadosComprobanteDTO));
  }
}
