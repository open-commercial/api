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
    nuevoRemitoDTO.setIdFacturaVenta(new long[]{1L});
    assertThrows(
        BusinessServiceException.class,
        () -> remitoService.crearRemitoDeFacturasVenta(nuevoRemitoDTO, 1L));
    verify(messageSource).getMessage(eq("mensaje_remito_sin_costo_de_envio"), any(), any());
    nuevoRemitoDTO.setCostoDeEnvio(new BigDecimal("50"));
    assertThrows(
        BusinessServiceException.class,
        () -> remitoService.crearRemitoDeFacturasVenta(nuevoRemitoDTO, 1L));
    verify(messageSource).getMessage(eq("mensaje_tipo_de_comprobante_no_valido"), any(), any());
    FacturaVenta facturaVentaUno = new FacturaVenta();
    facturaVentaUno.setIdFactura(2L);
    Sucursal sucursal = new Sucursal();
    sucursal.setIdSucursal(1L);
    sucursal.setNombre("primera sucursal");
    ConfiguracionSucursal configuracionSucursal = new ConfiguracionSucursal();
    configuracionSucursal.setNroPuntoDeVentaAfip(1);
    when(configuracionSucursalService.getConfiguracionSucursal(sucursal))
        .thenReturn(configuracionSucursal);
    facturaVentaUno.setSucursal(sucursal);
    facturaVentaUno.setTotal(new BigDecimal("100"));
    Pedido pedido = new Pedido();
    UbicacionDTO ubicacionDTO = UbicacionDTO.builder().build();
    pedido.setDetalleEnvio(ubicacionDTO);
    facturaVentaUno.setPedido(pedido);
    Cliente cliente = new Cliente();
    facturaVentaUno.setCliente(cliente);
    facturaVentaUno.setTipoComprobante(TipoDeComprobante.NOTA_CREDITO_C);
    when(facturaService.getFacturaNoEliminadaPorId(2L)).thenReturn(facturaVentaUno);
    nuevoRemitoDTO.setIdFacturaVenta(new long[]{2L, 3L});
    BigDecimal[] cantidadesDeBultos = new BigDecimal[] {new BigDecimal("6"), BigDecimal.TEN};
    TipoBulto[] tipoBulto = new TipoBulto[] {TipoBulto.CAJA};
    nuevoRemitoDTO.setCantidadPorBulto(cantidadesDeBultos);
    nuevoRemitoDTO.setTiposDeBulto(tipoBulto);
    Usuario usuario = new Usuario();
    when(usuarioService.getUsuarioNoEliminadoPorId(1L)).thenReturn(usuario);
    FacturaVenta facturaVentaDos = new FacturaVenta();
    facturaVentaDos.setIdFactura(3L);
    Sucursal sucursalDos = new Sucursal();
    sucursalDos.setIdSucursal(2L);
    sucursalDos.setNombre("segunda sucursal");
    ConfiguracionSucursal configuracionSucursalDos = new ConfiguracionSucursal();
    configuracionSucursalDos.setNroPuntoDeVentaAfip(1);
    when(configuracionSucursalService.getConfiguracionSucursal(sucursal))
            .thenReturn(configuracionSucursalDos);
    facturaVentaDos.setSucursal(sucursalDos);
    facturaVentaDos.setTotal(new BigDecimal("100"));
    Pedido pedidoDos = new Pedido();
    UbicacionDTO ubicacionDos = UbicacionDTO.builder()
            .descripcion("Chaco").build();
    pedidoDos.setDetalleEnvio(ubicacionDos);
    facturaVentaDos.setPedido(pedidoDos);
    Cliente clienteDos = new Cliente();
    clienteDos.setNombreFiscal("segundo cliente");
    facturaVentaDos.setCliente(clienteDos);
    facturaVentaDos.setTipoComprobante(TipoDeComprobante.NOTA_CREDITO_C);
    when(facturaService.getFacturaNoEliminadaPorId(3L)).thenReturn(facturaVentaDos);
    assertThrows(
            BusinessServiceException.class,
            () -> remitoService.crearRemitoDeFacturasVenta(nuevoRemitoDTO, 1L));
    verify(messageSource).getMessage(eq("mensaje_remito_facturas_diferentes_clientes"), any(), any());
    facturaVentaDos.setCliente(cliente);
    assertThrows(
            BusinessServiceException.class,
            () -> remitoService.crearRemitoDeFacturasVenta(nuevoRemitoDTO, 1L));
    verify(messageSource).getMessage(eq("mensaje_remito_facturas_diferentes_sucursales"), any(), any());
    facturaVentaDos.setSucursal(sucursal);
    assertThrows(
            BusinessServiceException.class,
            () -> remitoService.crearRemitoDeFacturasVenta(nuevoRemitoDTO, 1L));
    verify(messageSource).getMessage(eq("mensaje_remito_facturas_diferentes_ubicacion_envio"), any(), any());
    facturaVentaDos.setPedido(pedido);
    assertThrows(
            BusinessServiceException.class,
            () -> remitoService.crearRemitoDeFacturasVenta(nuevoRemitoDTO, 1L));
    verify(messageSource).getMessage(eq("mensaje_remito_error_renglones"), any(), any());
    tipoBulto = new TipoBulto[] {TipoBulto.CAJA, TipoBulto.ATADO};
    nuevoRemitoDTO.setTiposDeBulto(tipoBulto);
    Remito remito = remitoService.crearRemitoDeFacturasVenta(nuevoRemitoDTO, 1L);
    verify(remitoRepository).save(any());
    assertEquals(1, remito.getSerie());
    assertEquals(1, remito.getNroRemito());
    assertEquals(cliente, remito.getCliente());
    assertEquals(sucursal, remito.getSucursal());
    assertEquals(usuario, remito.getUsuario());
    assertEquals(2, remito.getRenglones().size());
    assertEquals(TipoBulto.CAJA.toString(), remito.getRenglones().get(0).getTipoBulto());
    assertEquals(new BigDecimal("6"), remito.getRenglones().get(0).getCantidad());
    assertEquals(TipoBulto.ATADO.toString(), remito.getRenglones().get(1).getTipoBulto());
    assertEquals(BigDecimal.TEN, remito.getRenglones().get(1).getCantidad());
    assertEquals(new BigDecimal("50"), remito.getCostoDeEnvio());
    assertEquals(new BigDecimal("200"), remito.getTotalFacturas());
    assertEquals(new BigDecimal("250"), remito.getTotal());
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
    when(remitoRepository.buscarMayorNumRemitoSegunSerie(1L))
        .thenReturn(null);
    assertEquals(1L, remitoService.getSiguienteNumeroRemito(1L));
    when(remitoRepository.buscarMayorNumRemitoSegunSerie(1L))
        .thenReturn(43L);
    assertEquals(44L, remitoService.getSiguienteNumeroRemito(1L));
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
            .idCliente(1L)
            .idSucursal(1L)
            .idUsuario(1L)
            .idTransportista(5L)
            .build();
    BooleanBuilder builder = remitoService.getBuilder(criteria);
    assertEquals(
        "remito.fecha between -999999999-01-01T00:00 and +999999999-12-31T23:59:59.999999999 "
            + "&& remito.serie = 1 && remito.nroRemito = 2 "
            + "&& remito.cliente.idCliente = 1 && remito.sucursal.idSucursal = 1 && remito.usuario.idUsuario = 1 "
            + "&& remito.transportista.idTransportista = 5 && remito.eliminado = false",
        builder.toString());
    criteria =
        BusquedaRemitoCriteria.builder()
            .fechaDesde(LocalDateTime.MIN)
            .serieRemito(1L)
            .nroRemito(2L)
            .idCliente(1L)
            .idSucursal(1L)
            .idUsuario(1L)
            .build();
    builder = remitoService.getBuilder(criteria);
    assertEquals(
        "remito.fecha > -999999999-01-01T00:00 && remito.serie = 1 " +
                "&& remito.nroRemito = 2 " +
                "&& remito.cliente.idCliente = 1 && remito.sucursal.idSucursal = 1 " +
                "&& remito.usuario.idUsuario = 1 && remito.eliminado = false",
        builder.toString());
    criteria =
        BusquedaRemitoCriteria.builder()
            .fechaHasta(LocalDateTime.MAX)
            .serieRemito(1L)
            .nroRemito(2L)
            .idCliente(1L)
            .idSucursal(1L)
            .idUsuario(1L)
            .serieFacturaVenta(2L)
            .nroFacturaVenta(123L)
            .build();
    builder = remitoService.getBuilder(criteria);
    assertEquals(
        "remito.fecha < +999999999-12-31T23:59:59.999999999 && remito.serie = 1 " +
                "&& remito.nroRemito = 2 && remito.cliente.idCliente = 1 " +
                "&& remito.sucursal.idSucursal = 1 && remito.usuario.idUsuario = 1 && remito.eliminado = false",
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
}
