package sic.service.impl;

import com.querydsl.core.BooleanBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import sic.exception.BusinessServiceException;
import sic.modelo.*;
import sic.modelo.criteria.BusquedaRemitoCriteria;
import sic.modelo.dto.NuevoRemitoDTO;
import sic.modelo.dto.UbicacionDTO;
import sic.repository.RemitoRepository;
import sic.repository.RenglonRemitoRepository;
import sic.service.*;
import sic.util.CustomValidator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
    classes = {CustomValidator.class, RemitoServiceImpl.class, MessageSource.class})
class RemitoServiceImplTest {

  @MockBean IFacturaService facturaService;
  @MockBean IFacturaVentaService facturaVentaService;
  @MockBean RemitoRepository remitoRepository;
  @MockBean RenglonRemitoRepository renglonRemitoRepository;
  @MockBean IClienteService clienteService;
  @MockBean IUsuarioService usuarioService;
  @MockBean ITransportistaService transportistaService;
  @MockBean IConfiguracionSucursalService configuracionSucursalService;
  @MockBean ICuentaCorrienteService cuentaCorrienteService;
  @MockBean ISucursalService sucursalService;
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
    Transportista transportista = new Transportista();
    transportista.setIdTransportista(1L);
    transportista.setNombre("Transportista Test");
    when(transportistaService.getTransportistaNoEliminadoPorId(1L)).thenReturn(transportista);
    NuevoRemitoDTO nuevoRemitoDTO = NuevoRemitoDTO.builder().build();
    nuevoRemitoDTO.setIdTransportista(1L);
    nuevoRemitoDTO.setPesoTotalEnKg(BigDecimal.TEN);
    nuevoRemitoDTO.setVolumenTotalEnM3(BigDecimal.ONE);
    nuevoRemitoDTO.setObservaciones("Envio Nuevo");
    nuevoRemitoDTO.setIdFacturaVenta(1L);
    assertThrows(
        BusinessServiceException.class,
        () -> remitoService.crearRemitoDeFacturaVenta(nuevoRemitoDTO, 1L));
    verify(messageSource).getMessage(eq("mensaje_remito_sin_costo_de_envio"), any(), any());
    nuevoRemitoDTO.setCostoDeEnvio(new BigDecimal("50"));
    assertThrows(
        BusinessServiceException.class,
        () -> remitoService.crearRemitoDeFacturaVenta(nuevoRemitoDTO, 1L));
    verify(messageSource).getMessage(eq("mensaje_tipo_de_comprobante_no_valido"), any(), any());
    FacturaVenta facturaVenta = new FacturaVenta();
    facturaVenta.setIdFactura(2L);
    Sucursal sucursal = new Sucursal();
    sucursal.setIdSucursal(1L);
    sucursal.setNombre("primera sucursal");
    ConfiguracionSucursal configuracionSucursal = new ConfiguracionSucursal();
    configuracionSucursal.setNroPuntoDeVentaAfip(1);
    sucursal.setConfiguracionSucursal(configuracionSucursal);
    facturaVenta.setSucursal(sucursal);
    facturaVenta.setTotal(new BigDecimal("100"));
    Pedido pedido = new Pedido();
    UbicacionDTO ubicacionDTO = UbicacionDTO.builder().build();
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
    BigDecimal[] cantidadesDeBultos = new BigDecimal[] {new BigDecimal("6"), BigDecimal.TEN};
    TipoBulto[] tipoBulto = new TipoBulto[] {TipoBulto.CAJA};
    nuevoRemitoDTO.setCantidadPorBulto(cantidadesDeBultos);
    nuevoRemitoDTO.setTiposDeBulto(tipoBulto);
    assertThrows(
        BusinessServiceException.class,
        () -> remitoService.crearRemitoDeFacturaVenta(nuevoRemitoDTO, 1L));
    verify(messageSource).getMessage(eq("mensaje_remito_error_renglones"), any(), any());
    tipoBulto = new TipoBulto[] {TipoBulto.CAJA, TipoBulto.ATADO};
    nuevoRemitoDTO.setTiposDeBulto(tipoBulto);
    Usuario usuario = new Usuario();
    when(usuarioService.getUsuarioNoEliminadoPorId(1L)).thenReturn(usuario);
    Remito remito = remitoService.crearRemitoDeFacturaVenta(nuevoRemitoDTO, 1L);
    verify(remitoRepository).save(any());
    assertEquals(1, remito.getSerie());
    assertEquals(1, remito.getNroRemito());
    assertEquals(TipoDeComprobante.REMITO_A, remito.getTipoComprobante());
    assertEquals(cliente, remito.getCliente());
    assertEquals(sucursal, remito.getSucursal());
    assertEquals(usuario, remito.getUsuario());
    assertEquals(2, remito.getRenglones().size());
    assertEquals(TipoBulto.CAJA.toString(), remito.getRenglones().get(0).getTipoBulto());
    assertEquals(new BigDecimal("6"), remito.getRenglones().get(0).getCantidad());
    assertEquals(TipoBulto.ATADO.toString(), remito.getRenglones().get(1).getTipoBulto());
    assertEquals(BigDecimal.TEN, remito.getRenglones().get(1).getCantidad());
    assertEquals(new BigDecimal("50"), remito.getCostoDeEnvio());
    assertEquals(new BigDecimal("100"), remito.getTotalFactura());
    assertEquals(new BigDecimal("150"), remito.getTotal());
    assertEquals(BigDecimal.TEN, remito.getPesoTotalEnKg());
    assertEquals(BigDecimal.ONE, remito.getVolumenTotalEnM3());
    assertEquals("Envio Nuevo", remito.getObservaciones());
    assertEquals(new BigDecimal("16"), remito.getCantidadDeBultos());
    verify(messageSource)
        .getMessage(eq("mensaje_remito_guardado_correctamente"), any(), eq(Locale.getDefault()));
    verify(facturaVentaService).asignarRemitoConFactura(any(), eq(2L));
    verify(cuentaCorrienteService)
        .asentarEnCuentaCorriente((Remito) any(), eq(TipoDeOperacion.ALTA));
  }

  @Test
  void shouldConstruirRenglonesDeRemito() {
    TipoBulto[] tipoBulto = new TipoBulto[] {TipoBulto.CAJA};
    BigDecimal[] cantidadesDeBultos = new BigDecimal[] {BigDecimal.TEN, BigDecimal.ONE};
    NuevoRemitoDTO nuevoRemitoDTO =
        NuevoRemitoDTO.builder()
            .tiposDeBulto(tipoBulto)
            .cantidadPorBulto(cantidadesDeBultos)
            .build();
    assertThrows(
        BusinessServiceException.class,
        () -> remitoService.construirRenglonesDeRemito(nuevoRemitoDTO));
    verify(messageSource).getMessage(eq("mensaje_remito_error_renglones"), any(), any());
    cantidadesDeBultos = new BigDecimal[] {BigDecimal.TEN};
    nuevoRemitoDTO.setCantidadPorBulto(cantidadesDeBultos);
    List<RenglonRemito> renglonesParaRemito =
        remitoService.construirRenglonesDeRemito(nuevoRemitoDTO);
    assertNotNull(renglonesParaRemito);
    assertEquals(1, cantidadesDeBultos.length);
    assertEquals(new BigDecimal("10"), renglonesParaRemito.get(0).getCantidad());
    assertEquals(TipoBulto.CAJA.toString(), renglonesParaRemito.get(0).getTipoBulto());
  }

  @Test
  void shouldEliminarRemito() {
    Remito remito = new Remito();
    when(remitoRepository.findById(1L)).thenReturn(Optional.of(remito));
    FacturaVenta facturaVenta = new FacturaVenta();
    facturaVenta.setIdFactura(1L);
    when(facturaVentaService.getFacturaVentaDelRemito(remito)).thenReturn(facturaVenta);
    remitoService.eliminar(1L);
    verify(cuentaCorrienteService).asentarEnCuentaCorriente(remito, TipoDeOperacion.ELIMINACION);
    verify(facturaVentaService).asignarRemitoConFactura(eq(null), eq(1L));
    verify(remitoRepository).save(remito);
    verify(messageSource).getMessage(eq("mensaje_remito_eliminado_correctamente"), any(), any());
  }

  @Test
  void shouldGetSiguienteNumeroRemito() {
    when(remitoRepository.buscarMayorNumRemitoSegunTipo(TipoDeComprobante.REMITO_A, 1L))
        .thenReturn(null);
    assertEquals(1L, remitoService.getSiguienteNumeroRemito(TipoDeComprobante.REMITO_A, 1L));
    when(remitoRepository.buscarMayorNumRemitoSegunTipo(TipoDeComprobante.REMITO_A, 1L))
        .thenReturn(43L);
    assertEquals(44L, remitoService.getSiguienteNumeroRemito(TipoDeComprobante.REMITO_A, 1L));
  }

  @Test
  void shouldGetRenglonesDelRemito() {
    remitoService.getRenglonesDelRemito(1L);
    verify(renglonRemitoRepository).findByIdRemitoOrderByIdRenglonRemito(1L);
  }

  @Test
  void shouldBuscarRemito() {
    BusquedaRemitoCriteria criteria =
        BusquedaRemitoCriteria.builder()
            .fechaDesde(LocalDateTime.MIN)
            .fechaHasta(LocalDateTime.MAX)
            .serieRemito(1L)
            .nroRemito(2L)
            .tipoDeRemito(TipoDeComprobante.REMITO_A)
            .idCliente(1L)
            .idSucursal(1L)
            .idUsuario(1L)
            .idTransportista(5L)
            .build();
    BooleanBuilder builder = remitoService.getBuilder(criteria);
    assertEquals(
        "remito.fecha between -999999999-01-01T00:00 and +999999999-12-31T23:59:59.999999999 "
            + "&& remito.serie = 1 && remito.nroRemito = 2 && remito.tipoComprobante = REMITO_A "
            + "&& remito.cliente.idCliente = 1 && remito.sucursal.idSucursal = 1 && remito.usuario.idUsuario = 1 "
            + "&& remito.transportista.idTransportista = 5 && remito.eliminado = false",
        builder.toString());
    criteria =
        BusquedaRemitoCriteria.builder()
            .fechaDesde(LocalDateTime.MIN)
            .serieRemito(1L)
            .nroRemito(2L)
            .tipoDeRemito(TipoDeComprobante.REMITO_A)
            .idCliente(1L)
            .idSucursal(1L)
            .idUsuario(1L)
            .build();
    builder = remitoService.getBuilder(criteria);
    assertEquals(
        "remito.fecha > -999999999-01-01T00:00 && remito.serie = 1 " +
                "&& remito.nroRemito = 2 && remito.tipoComprobante = REMITO_A " +
                "&& remito.cliente.idCliente = 1 && remito.sucursal.idSucursal = 1 " +
                "&& remito.usuario.idUsuario = 1 && remito.eliminado = false",
        builder.toString());
    criteria =
        BusquedaRemitoCriteria.builder()
            .fechaHasta(LocalDateTime.MAX)
            .serieRemito(1L)
            .nroRemito(2L)
            .tipoDeRemito(TipoDeComprobante.REMITO_A)
            .idCliente(1L)
            .idSucursal(1L)
            .idUsuario(1L)
            .serieFacturaVenta(2L)
            .nroFacturaVenta(123L)
            .build();
    builder = remitoService.getBuilder(criteria);
    assertEquals(
        "remito.fecha < +999999999-12-31T23:59:59.999999999 " +
                "&& remito.serie = 1 && remito.nroRemito = 2 " +
                "&& remito.tipoComprobante = REMITO_A && remito.cliente.idCliente = 1 " +
                "&& remito.sucursal.idSucursal = 1 && remito.usuario.idUsuario = 1 " +
                "&& remito.facturaVenta.numSerie = 2 && remito.facturaVenta.numFactura = 123 " +
                "&& remito.eliminado = false",
        builder.toString());
    Pageable pageable = remitoService.getPageable(3, "cliente.razonSocial", "ASC");
    assertEquals(3, pageable.getPageNumber());
    assertEquals("cliente.razonSocial: ASC", pageable.getSort().toString());
    pageable = remitoService.getPageable(3, "cliente.razonSocial", "DESC");
    assertEquals(3, pageable.getPageNumber());
    assertEquals("cliente.razonSocial: DESC", pageable.getSort().toString());
    pageable = remitoService.getPageable(null, null, null);
    assertEquals(0, pageable.getPageNumber());
    assertEquals("fecha: DESC", pageable.getSort().toString());
    remitoService.buscarRemito(criteria);
    verify(remitoRepository).findAll(eq(builder), eq(pageable));
  }

  @Test
  void shouldTestGetTipoDeRemitos() {
    Sucursal sucursal = new Sucursal();
    sucursal.setCategoriaIVA(CategoriaIVA.RESPONSABLE_INSCRIPTO);
    when(sucursalService.getSucursalPorId(1L)).thenReturn(sucursal);
    TipoDeComprobante[] tipoDeComprobantes = remitoService.getTiposDeComprobanteSegunSucursal(1L);
    assertEquals(4, tipoDeComprobantes.length);
    assertEquals(TipoDeComprobante.REMITO_A, tipoDeComprobantes[0]);
    assertEquals(TipoDeComprobante.REMITO_B, tipoDeComprobantes[1]);
    assertEquals(TipoDeComprobante.REMITO_X, tipoDeComprobantes[2]);
    assertEquals(TipoDeComprobante.REMITO_PRESUPUESTO, tipoDeComprobantes[3]);
    sucursal.setCategoriaIVA(CategoriaIVA.MONOTRIBUTO);
    when(sucursalService.getSucursalPorId(1L)).thenReturn(sucursal);
    tipoDeComprobantes = remitoService.getTiposDeComprobanteSegunSucursal(1L);
    assertEquals(3, tipoDeComprobantes.length);
    assertEquals(TipoDeComprobante.REMITO_C, tipoDeComprobantes[0]);
    assertEquals(TipoDeComprobante.REMITO_X, tipoDeComprobantes[1]);
    assertEquals(TipoDeComprobante.REMITO_PRESUPUESTO, tipoDeComprobantes[2]);
  }
}
