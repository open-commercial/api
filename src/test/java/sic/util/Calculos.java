package sic.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import sic.modelo.dto.NuevoRenglonFacturaDTO;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

@ExtendWith(SpringExtension.class)
public class Calculos {

  @Test
  void shouldGetArrayDeIdProductoParaFactura() {
    long[] arrayEsperado = {1L, 2L, 3L};
    List<NuevoRenglonFacturaDTO> nuevosRenglonsFactura = new ArrayList<>();
    nuevosRenglonsFactura.add(NuevoRenglonFacturaDTO.builder().idProducto(1L).build());
    nuevosRenglonsFactura.add(NuevoRenglonFacturaDTO.builder().idProducto(2L).build());
    nuevosRenglonsFactura.add(NuevoRenglonFacturaDTO.builder().idProducto(3L).build());
    assertArrayEquals(
        arrayEsperado, CalculosComprobante.getArrayDeIdProductoParaFactura(nuevosRenglonsFactura));
  }

  @Test
  void shouldGetArrayDeCantidadesParaFactura() {
    BigDecimal[] arrayEsperado = {new BigDecimal("10"), new BigDecimal("20"), new BigDecimal("5")};
    List<NuevoRenglonFacturaDTO> nuevosRenglonsFactura = new ArrayList<>();
    nuevosRenglonsFactura.add(
        NuevoRenglonFacturaDTO.builder().cantidad(new BigDecimal("10")).build());
    nuevosRenglonsFactura.add(
        NuevoRenglonFacturaDTO.builder().cantidad(new BigDecimal("20")).build());
    nuevosRenglonsFactura.add(
        NuevoRenglonFacturaDTO.builder().cantidad(new BigDecimal("5")).build());
    assertArrayEquals(
        arrayEsperado,
        CalculosComprobante.getArrayDeCantidadesProductoParaFactura(nuevosRenglonsFactura));
  }

  @Test
  void shouldGetArrayDeBonificacionesDeRenglonParaFactura() {
    BigDecimal[] arrayEsperado = {new BigDecimal("5"), new BigDecimal("2"), new BigDecimal("12")};
    List<NuevoRenglonFacturaDTO> nuevosRenglonsFactura = new ArrayList<>();
    nuevosRenglonsFactura.add(
        NuevoRenglonFacturaDTO.builder().bonificacion(new BigDecimal("5")).build());
    nuevosRenglonsFactura.add(
        NuevoRenglonFacturaDTO.builder().bonificacion(new BigDecimal("2")).build());
    nuevosRenglonsFactura.add(
        NuevoRenglonFacturaDTO.builder().bonificacion(new BigDecimal("12")).build());
    assertArrayEquals(
        arrayEsperado,
        CalculosComprobante.getArrayDeBonificacionesParaFactura(nuevosRenglonsFactura));
  }
}
