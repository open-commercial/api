package sic.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import org.springframework.test.context.junit.jupiter.SpringExtension;
import sic.builder.SucursalBuilder;
import sic.builder.RenglonNotaCreditoBuilder;
import sic.modelo.*;

@ExtendWith(SpringExtension.class)
class NotaServiceImplTest {

  @Mock private SucursalServiceImpl sucursalServiceImpl;
  @Mock private ClienteServiceImpl clienteService;
  @Mock private ProveedorServiceImpl proveedorService;
  @InjectMocks private NotaServiceImpl notaServiceImpl;

  @Test
  void shouldGetTipoNotaCreditoWhenSucursalYClienteDiscriminanIVA() {
    Sucursal sucursal = new Sucursal();
    sucursal.setCategoriaIVA(CategoriaIVA.RESPONSABLE_INSCRIPTO);
    Cliente cliente = new Cliente();
    cliente.setCategoriaIVA(CategoriaIVA.RESPONSABLE_INSCRIPTO);
    when(sucursalServiceImpl.getSucursalPorId(1L)).thenReturn(sucursal);
    when(clienteService.getClienteNoEliminadoPorId(1L)).thenReturn(cliente);
    TipoDeComprobante[] array = {
      TipoDeComprobante.NOTA_CREDITO_A,
      TipoDeComprobante.NOTA_CREDITO_X,
      TipoDeComprobante.NOTA_CREDITO_PRESUPUESTO,
    };
    List<TipoDeComprobante> expResult = Arrays.asList(array);
    List<TipoDeComprobante> result = notaServiceImpl.getTipoNotaCreditoCliente(1L, 1L);
    assertEquals(expResult, result);
  }

  @Test
  void shouldGetTipoNotaDebitoWhenSucursalYClienteDiscriminanIVA() {
    Sucursal sucursal = new Sucursal();
    sucursal.setCategoriaIVA(CategoriaIVA.RESPONSABLE_INSCRIPTO);
    Cliente cliente = new Cliente();
    cliente.setCategoriaIVA(CategoriaIVA.RESPONSABLE_INSCRIPTO);
    when(sucursalServiceImpl.getSucursalPorId(1L)).thenReturn(sucursal);
    when(clienteService.getClienteNoEliminadoPorId(1L)).thenReturn(cliente);
    TipoDeComprobante[] array = {
      TipoDeComprobante.NOTA_DEBITO_A,
      TipoDeComprobante.NOTA_DEBITO_X,
      TipoDeComprobante.NOTA_DEBITO_PRESUPUESTO
    };
    List<TipoDeComprobante> expResult = Arrays.asList(array);
    List<TipoDeComprobante> result =notaServiceImpl.getTipoNotaDebitoCliente(1L, 1L);
    assertEquals(expResult, result);
  }

  @Test
  void shouldGetTipoNotaCreditoWhenSucursalDiscriminaYClienteNoIVA() {
    Sucursal sucursal = new Sucursal();
    sucursal.setCategoriaIVA(CategoriaIVA.RESPONSABLE_INSCRIPTO);
    Cliente cliente = new Cliente();
    cliente.setCategoriaIVA(CategoriaIVA.CONSUMIDOR_FINAL);
    when(sucursalServiceImpl.getSucursalPorId(1L)).thenReturn(sucursal);
    when(clienteService.getClienteNoEliminadoPorId(1L)).thenReturn(cliente);
    TipoDeComprobante[] array = {
      TipoDeComprobante.NOTA_CREDITO_B,
      TipoDeComprobante.NOTA_CREDITO_X,
      TipoDeComprobante.NOTA_CREDITO_PRESUPUESTO,
    };
    List<TipoDeComprobante> expResult = Arrays.asList(array);
    List<TipoDeComprobante> result = notaServiceImpl.getTipoNotaCreditoCliente(1L, 1L);
    assertEquals(expResult, result);
  }

  @Test
  void shouldGetTipoNotaDebitoWhenSucursalDiscriminaYClienteNoIVA() {
    Sucursal sucursal = new Sucursal();
    sucursal.setCategoriaIVA(CategoriaIVA.RESPONSABLE_INSCRIPTO);
    Cliente cliente = new Cliente();
    cliente.setCategoriaIVA(CategoriaIVA.CONSUMIDOR_FINAL);
    when(sucursalServiceImpl.getSucursalPorId(1L)).thenReturn(sucursal);
    when(clienteService.getClienteNoEliminadoPorId(1L)).thenReturn(cliente);
    TipoDeComprobante[] array = {
      TipoDeComprobante.NOTA_DEBITO_B,
      TipoDeComprobante.NOTA_DEBITO_X,
      TipoDeComprobante.NOTA_DEBITO_PRESUPUESTO
    };
    List<TipoDeComprobante> expResult = Arrays.asList(array);
    List<TipoDeComprobante> result = notaServiceImpl.getTipoNotaDebitoCliente(1L, 1L);
    assertEquals(expResult, result);
  }

  @Test
  void shouldGetTipoNotaCreditoWhenSucursalNoDiscriminaYClienteSiIVA() {
    Sucursal sucursal = new Sucursal();
    sucursal.setCategoriaIVA(CategoriaIVA.MONOTRIBUTO);
    Cliente cliente = new Cliente();
    cliente.setCategoriaIVA(CategoriaIVA.RESPONSABLE_INSCRIPTO);
    when(sucursalServiceImpl.getSucursalPorId(1L)).thenReturn(sucursal);
    when(clienteService.getClienteNoEliminadoPorId(1L)).thenReturn(cliente);
    TipoDeComprobante[] array = {
      TipoDeComprobante.NOTA_CREDITO_C,
      TipoDeComprobante.NOTA_CREDITO_X,
      TipoDeComprobante.NOTA_CREDITO_PRESUPUESTO,
    };
    List<TipoDeComprobante> expResult = Arrays.asList(array);
    List<TipoDeComprobante> result = notaServiceImpl.getTipoNotaCreditoCliente(1L, 1L);
    assertEquals(expResult, result);
  }

  @Test
  void shouldGetTipoNotaDebitoWhenSucursalNoDiscriminaYClienteSiIVA() {
    Sucursal sucursal = new Sucursal();
    sucursal.setCategoriaIVA(CategoriaIVA.MONOTRIBUTO);
    Cliente cliente = new Cliente();
    cliente.setCategoriaIVA(CategoriaIVA.RESPONSABLE_INSCRIPTO);
    when(sucursalServiceImpl.getSucursalPorId(1L)).thenReturn(sucursal);
    when(clienteService.getClienteNoEliminadoPorId(1L)).thenReturn(cliente);
    TipoDeComprobante[] array = {
      TipoDeComprobante.NOTA_DEBITO_C,
      TipoDeComprobante.NOTA_DEBITO_X,
      TipoDeComprobante.NOTA_DEBITO_PRESUPUESTO
    };
    List<TipoDeComprobante> expResult = Arrays.asList(array);
    List<TipoDeComprobante> result = notaServiceImpl.getTipoNotaDebitoCliente(1L, 1L);
    assertEquals(expResult, result);
  }

  @Test
  void shouldGetTipoNotaCreditoWhenSucursalNoDiscriminaYClienteNoIVA() {
    Sucursal sucursal = new Sucursal();
    sucursal.setCategoriaIVA(CategoriaIVA.MONOTRIBUTO);
    Cliente cliente = new Cliente();
    cliente.setCategoriaIVA(CategoriaIVA.MONOTRIBUTO);
    when(sucursalServiceImpl.getSucursalPorId(1L)).thenReturn(sucursal);
    when(clienteService.getClienteNoEliminadoPorId(1L)).thenReturn(cliente);
    TipoDeComprobante[] array = {
      TipoDeComprobante.NOTA_CREDITO_C,
      TipoDeComprobante.NOTA_CREDITO_X,
      TipoDeComprobante.NOTA_CREDITO_PRESUPUESTO
    };
    List<TipoDeComprobante> expResult = Arrays.asList(array);
    List<TipoDeComprobante> result = notaServiceImpl.getTipoNotaCreditoCliente(1L, 1L);
    assertEquals(expResult, result);
  }

  @Test
  void shouldGetTipoNotaDebitoWhenSucursalNoDiscriminaYClienteNoIVA() {
    Sucursal sucursal = new Sucursal();
    sucursal.setCategoriaIVA(CategoriaIVA.MONOTRIBUTO);
    sucursal.setIdFiscal(1L);
    Cliente cliente = new Cliente();
    cliente.setCategoriaIVA(CategoriaIVA.MONOTRIBUTO);
    when(sucursalServiceImpl.getSucursalPorId(1L)).thenReturn(sucursal);
    when(clienteService.getClienteNoEliminadoPorId(1L)).thenReturn(cliente);
    TipoDeComprobante[] array = {
      TipoDeComprobante.NOTA_DEBITO_C,
      TipoDeComprobante.NOTA_DEBITO_X,
      TipoDeComprobante.NOTA_DEBITO_PRESUPUESTO
    };
    List<TipoDeComprobante> expResult = Arrays.asList(array);
    List<TipoDeComprobante> result = notaServiceImpl.getTipoNotaDebitoCliente(1L, 1L);
    assertEquals(expResult, result);
  }

  @Test
  void shouldCalcularTotalNotaCredito() {
    RenglonNotaCredito renglonNotaCredito = new RenglonNotaCredito();
    renglonNotaCredito.setImporteNeto(new BigDecimal("172.062"));
    List<RenglonNotaCredito> renglones = new ArrayList<>();
    renglones.add(renglonNotaCredito);
    assertEquals((new BigDecimal("172.062")).compareTo(notaServiceImpl.calcularTotalNota(renglones)), 0);
  }

  @Test
  void shouldGetTipoNotaCreditoWhenSucursalYProveedorDiscriminanIVA() {
    Sucursal sucursal = new Sucursal();
    sucursal.setCategoriaIVA(CategoriaIVA.RESPONSABLE_INSCRIPTO);
    Proveedor proveedor = new Proveedor();
    proveedor.setCategoriaIVA(CategoriaIVA.RESPONSABLE_INSCRIPTO);
    when(sucursalServiceImpl.getSucursalPorId(1L)).thenReturn(sucursal);
    when(proveedorService.getProveedorNoEliminadoPorId(1L)).thenReturn(proveedor);
    TipoDeComprobante[] array = {
      TipoDeComprobante.NOTA_CREDITO_A,
      TipoDeComprobante.NOTA_CREDITO_X,
      TipoDeComprobante.NOTA_CREDITO_PRESUPUESTO,
    };
    List<TipoDeComprobante> expResult = Arrays.asList(array);
    List<TipoDeComprobante> result = notaServiceImpl.getTipoNotaCreditoProveedor(1L, 1L);
    assertEquals(expResult, result);
  }

  @Test
  void shouldGetTipoNotaDebitoWhenSucursalYProveedorDiscriminanIVA() {
    Sucursal sucursal = new Sucursal();
    sucursal.setCategoriaIVA(CategoriaIVA.RESPONSABLE_INSCRIPTO);
    Proveedor proveedor = new Proveedor();
    proveedor.setCategoriaIVA(CategoriaIVA.RESPONSABLE_INSCRIPTO);
    when(sucursalServiceImpl.getSucursalPorId(1L)).thenReturn(sucursal);
    when(proveedorService.getProveedorNoEliminadoPorId(1L)).thenReturn(proveedor);
    TipoDeComprobante[] array = {
      TipoDeComprobante.NOTA_DEBITO_A,
      TipoDeComprobante.NOTA_DEBITO_X,
      TipoDeComprobante.NOTA_DEBITO_PRESUPUESTO
    };
    List<TipoDeComprobante> expResult = Arrays.asList(array);
    List<TipoDeComprobante> result = notaServiceImpl.getTipoNotaDebitoProveedor(1L, 1L);
    assertEquals(expResult, result);
  }

  @Test
  void shouldGetTipoNotaCreditoWhenSucursalDiscriminaYProveedorNoIVA() {
    Sucursal sucursal = new Sucursal();
    sucursal.setCategoriaIVA(CategoriaIVA.RESPONSABLE_INSCRIPTO);
    Proveedor proveedor = new Proveedor();
    proveedor.setCategoriaIVA(CategoriaIVA.CONSUMIDOR_FINAL);
    when(sucursalServiceImpl.getSucursalPorId(1L)).thenReturn(sucursal);
    when(proveedorService.getProveedorNoEliminadoPorId(1L)).thenReturn(proveedor);
    TipoDeComprobante[] array = {
      TipoDeComprobante.NOTA_CREDITO_C,
      TipoDeComprobante.NOTA_CREDITO_X,
      TipoDeComprobante.NOTA_CREDITO_PRESUPUESTO,
    };
    List<TipoDeComprobante> expResult = Arrays.asList(array);
    List<TipoDeComprobante> result = notaServiceImpl.getTipoNotaCreditoProveedor(1L, 1L);
    assertEquals(expResult, result);
  }

  @Test
  void shouldGetTipoNotaDebitoWhenSucursalDiscriminaYProveedorNoIVA() {
    Sucursal sucursal = new Sucursal();
    sucursal.setCategoriaIVA(CategoriaIVA.RESPONSABLE_INSCRIPTO);
    Proveedor proveedor = new Proveedor();
    proveedor.setCategoriaIVA(CategoriaIVA.CONSUMIDOR_FINAL);
    when(sucursalServiceImpl.getSucursalPorId(1L)).thenReturn(sucursal);
    when(proveedorService.getProveedorNoEliminadoPorId(1L)).thenReturn(proveedor);
    TipoDeComprobante[] array = {
      TipoDeComprobante.NOTA_DEBITO_C,
      TipoDeComprobante.NOTA_DEBITO_X,
      TipoDeComprobante.NOTA_DEBITO_PRESUPUESTO
    };
    List<TipoDeComprobante> expResult = Arrays.asList(array);
    List<TipoDeComprobante> result = notaServiceImpl.getTipoNotaDebitoProveedor(1L, 1L);
    assertEquals(expResult, result);
  }

  @Test
  void shouldGetTipoNotaCreditoWhenSucursalNoDiscriminaYProveedorSiIVA() {
    Sucursal sucursal = new Sucursal();
    sucursal.setCategoriaIVA(CategoriaIVA.CONSUMIDOR_FINAL);
    Proveedor proveedor = new Proveedor();
    proveedor.setCategoriaIVA(CategoriaIVA.RESPONSABLE_INSCRIPTO);
    sucursal.setCategoriaIVA(CategoriaIVA.MONOTRIBUTO);
    when(sucursalServiceImpl.getSucursalPorId(1L)).thenReturn(sucursal);
    when(proveedorService.getProveedorNoEliminadoPorId(1L)).thenReturn(proveedor);
    TipoDeComprobante[] array = {
      TipoDeComprobante.NOTA_CREDITO_B,
      TipoDeComprobante.NOTA_CREDITO_X,
      TipoDeComprobante.NOTA_CREDITO_PRESUPUESTO,
    };
    List<TipoDeComprobante> expResult = Arrays.asList(array);
    List<TipoDeComprobante> result = notaServiceImpl.getTipoNotaCreditoProveedor(1L, 1L);
    assertEquals(expResult, result);
  }

  @Test
  void shouldGetTipoNotaDebitoWhenSucursalNoDiscriminaYProveedorSiIVA() {
    Sucursal sucursal = new Sucursal();
    sucursal.setCategoriaIVA(CategoriaIVA.MONOTRIBUTO);
    Proveedor proveedor = new Proveedor();
    proveedor.setCategoriaIVA(CategoriaIVA.RESPONSABLE_INSCRIPTO);
    when(sucursalServiceImpl.getSucursalPorId(1L)).thenReturn(sucursal);
    when(proveedorService.getProveedorNoEliminadoPorId(1L)).thenReturn(proveedor);
    TipoDeComprobante[] array = {
      TipoDeComprobante.NOTA_DEBITO_B,
      TipoDeComprobante.NOTA_DEBITO_X,
      TipoDeComprobante.NOTA_DEBITO_PRESUPUESTO
    };
    List<TipoDeComprobante> expResult = Arrays.asList(array);
    List<TipoDeComprobante> result = notaServiceImpl.getTipoNotaDebitoProveedor(1L, 1L);
    assertEquals(expResult, result);
  }

  @Test
  void shouldGetTipoNotaCreditoWhenSucursalNoDiscriminaYProveedorNoIVA() {
    Sucursal sucursal = new Sucursal();
    sucursal.setCategoriaIVA(CategoriaIVA.MONOTRIBUTO);
    Proveedor proveedor = new Proveedor();
    proveedor.setCategoriaIVA(CategoriaIVA.MONOTRIBUTO);
    when(sucursalServiceImpl.getSucursalPorId(1L)).thenReturn(sucursal);
    when(proveedorService.getProveedorNoEliminadoPorId(1L)).thenReturn(proveedor);
    TipoDeComprobante[] array = {
      TipoDeComprobante.NOTA_CREDITO_C,
      TipoDeComprobante.NOTA_CREDITO_X,
      TipoDeComprobante.NOTA_CREDITO_PRESUPUESTO
    };
    List<TipoDeComprobante> expResult = Arrays.asList(array);
    List<TipoDeComprobante> result = notaServiceImpl.getTipoNotaCreditoProveedor(1L, 1L);
    assertEquals(expResult, result);
  }

  @Test
  void shouldGetTipoNotaDebitoWhenSucursalNoDiscriminaYProveedorNoIVA() {
    Sucursal sucursal = new Sucursal();
    sucursal.setCategoriaIVA(CategoriaIVA.MONOTRIBUTO);
    Proveedor proveedor = new Proveedor();
    proveedor.setCategoriaIVA(CategoriaIVA.MONOTRIBUTO);
    when(sucursalServiceImpl.getSucursalPorId(1L)).thenReturn(sucursal);
    when(proveedorService.getProveedorNoEliminadoPorId(1L)).thenReturn(proveedor);
    TipoDeComprobante[] array = {
      TipoDeComprobante.NOTA_DEBITO_C,
      TipoDeComprobante.NOTA_DEBITO_X,
      TipoDeComprobante.NOTA_DEBITO_PRESUPUESTO
    };
    List<TipoDeComprobante> expResult = Arrays.asList(array);
    List<TipoDeComprobante> result = notaServiceImpl.getTipoNotaDebitoProveedor(1L, 1L);
    assertEquals(expResult, result);
  }
}
