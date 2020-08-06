package sic.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import sic.exception.BusinessServiceException;
import sic.modelo.*;
import sic.modelo.dto.NuevoRemitoDTO;
import sic.modelo.dto.UbicacionDTO;
import sic.repository.RemitoRepository;
import sic.repository.RenglonRemitoRepository;
import sic.service.*;
import sic.util.CustomValidator;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CustomValidator.class, RemitoServiceImpl.class, MessageSource.class})
class RemitoServiceImplTest {

  @MockBean IFacturaService facturaService;
  @MockBean IFacturaVentaService facturaVentaService;
  @MockBean RemitoRepository remitoRepository;
  @MockBean RenglonRemitoRepository renglonRemitoRepository;
  @MockBean IClienteService clienteService;
  @MockBean IUsuarioService usuarioService;
  @MockBean IConfiguracionSucursalService configuracionSucursalService;
  @MockBean ICuentaCorrienteService cuentaCorrienteService;
  @MockBean MessageSource messageSource;

  @Autowired RemitoServiceImpl remitoService;

  @Test
  void shouldGetRemitoPorId() {
    Remito remito = new Remito();
    when(remitoRepository.findById(1L)).thenReturn(Optional.of(remito));
    Remito remitoRecuperado = remitoService.getRemitoPorId(1L);
    assertNotNull(remitoRecuperado);
  }

  @Test
  void shouldCrearRemitoDeFacturaVenta() {
    Factura factura = new FacturaCompra();
    when(facturaService.getFacturaNoEliminadaPorId(1L)).thenReturn(factura);
    NuevoRemitoDTO nuevoRemitoDTO = NuevoRemitoDTO.builder().build();
    nuevoRemitoDTO.setIdFacturaVenta(1L);
    assertThrows(BusinessServiceException.class, () -> remitoService.crearRemitoDeFacturaVenta(nuevoRemitoDTO, 1L));
    verify(messageSource).getMessage(eq("mensaje_tipo_de_comprobante_no_valido"), any(), any());
    FacturaVenta facturaVenta = new FacturaVenta();
    facturaVenta.setIdFactura(2L);
    Sucursal sucursal = new Sucursal();
    sucursal.setIdSucursal(1L);
    sucursal.setNombre("primera sucursal");
    ConfiguracionSucursal configuracionSucursal = new ConfiguracionSucursal();
    configuracionSucursal.setNroPuntoDeVentaAfip(1);
    when(configuracionSucursalService.getConfiguracionSucursal(sucursal))
        .thenReturn(configuracionSucursal);
    facturaVenta.setSucursal(sucursal);
    Pedido pedido = new Pedido();
    UbicacionDTO ubicacionDTO = UbicacionDTO.builder().costoDeEnvio(new BigDecimal("100")).build();
    pedido.setDetalleEnvio(ubicacionDTO);
    facturaVenta.setPedido(pedido);
    Cliente cliente = new Cliente();
    facturaVenta.setCliente(cliente);
    facturaVenta.setTipoComprobante(TipoDeComprobante.NOTA_CREDITO_C);
    when(facturaService.getFacturaNoEliminadaPorId(2L)).thenReturn(facturaVenta);
    nuevoRemitoDTO.setIdFacturaVenta(2L);
    assertThrows(
        BusinessServiceException.class,
        () -> remitoService.crearRemitoDeFacturaVenta(nuevoRemitoDTO, 1L));
    verify(messageSource).getMessage(eq("mensaje_remito_tipo_no_valido"), any(), any());
    facturaVenta.setTipoComprobante(TipoDeComprobante.FACTURA_A);
    nuevoRemitoDTO.setDividir(true);
    BigDecimal[] cantidadesDeBultos = new BigDecimal[] {new BigDecimal("6"), BigDecimal.TEN};
    TipoBulto[] tipoBulto =
        new TipoBulto[] {TipoBulto.CAJA};
    nuevoRemitoDTO.setCantidadDeBultos(cantidadesDeBultos);
    nuevoRemitoDTO.setTiposDeBulto(tipoBulto);
    assertThrows(
        BusinessServiceException.class,
        () -> remitoService.crearRemitoDeFacturaVenta(nuevoRemitoDTO, 1L));
    verify(messageSource).getMessage(eq("mensaje_remito_error_renglones"), any(), any());
    tipoBulto = new TipoBulto[]{TipoBulto.CAJA, TipoBulto.ATADO};
    nuevoRemitoDTO.setTiposDeBulto(tipoBulto);
    Usuario usuario = new Usuario();
    when(usuarioService.getUsuarioNoEliminadoPorId(1L)).thenReturn(usuario);
    remitoService.crearRemitoDeFacturaVenta(nuevoRemitoDTO, 1L);
    verify(remitoRepository).save(any());
    verify(messageSource).getMessage(eq("mensaje_remito_guardado_correctamente"), any(), eq(Locale.getDefault()));
    verify(facturaVentaService).asignarRemitoConFactura(any(), eq(2L));
    verify(cuentaCorrienteService).asentarEnCuentaCorriente((Remito) any(), eq(TipoDeOperacion.ALTA));
  }
}
