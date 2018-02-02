package sic.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.when;
import org.springframework.test.context.junit4.SpringRunner;
import sic.builder.ClienteBuilder;
import sic.builder.EmpresaBuilder;
import sic.builder.TransportistaBuilder;
import sic.modelo.Cliente;
import sic.modelo.CondicionIVA;
import sic.modelo.Empresa;
import sic.modelo.Factura;
import sic.modelo.FacturaVenta;
import sic.modelo.Medida;
import sic.modelo.Producto;
import sic.modelo.Proveedor;
import sic.modelo.RenglonFactura;
import sic.modelo.Movimiento;
import sic.modelo.TipoDeComprobante;
import sic.modelo.Usuario;
import sic.repository.FacturaVentaRepository;

@RunWith(SpringRunner.class)
public class FacturaServiceImplTest {
       
    @Mock
    private FacturaVentaRepository facturaVentaRepository;  
    
    @Mock
    private ProductoServiceImpl productoService;
    
    @InjectMocks
    private FacturaServiceImpl facturaService;

    @Test
    public void shouldGetTipoFacturaCompraWhenEmpresaYProveedorDiscriminanIVA() {
        Empresa empresa = Mockito.mock(Empresa.class);
        CondicionIVA condicionIVAqueDiscrimina = Mockito.mock(CondicionIVA.class);
        when(condicionIVAqueDiscrimina.isDiscriminaIVA()).thenReturn(Boolean.TRUE);
        when(empresa.getCondicionIVA()).thenReturn(condicionIVAqueDiscrimina);
        empresa.setCondicionIVA(condicionIVAqueDiscrimina);
        Proveedor proveedor = Mockito.mock(Proveedor.class);
        when(proveedor.getCondicionIVA()).thenReturn(condicionIVAqueDiscrimina);
        TipoDeComprobante[] expResult = new TipoDeComprobante[4];
        expResult[0] = TipoDeComprobante.FACTURA_A;
        expResult[1] = TipoDeComprobante.FACTURA_B;
        expResult[2] = TipoDeComprobante.FACTURA_X;
        expResult[3] = TipoDeComprobante.PRESUPUESTO;
        TipoDeComprobante[] result = facturaService.getTipoFacturaCompra(empresa, proveedor);
        assertArrayEquals(expResult, result);
    }

    @Test
    public void shouldGetTipoFacturaCompraWhenEmpresaDiscriminaIVAYProveedorNO() {
        Empresa empresa = Mockito.mock(Empresa.class);
        Proveedor proveedor = Mockito.mock(Proveedor.class);
        CondicionIVA condicionIVAqueDiscrimina = Mockito.mock(CondicionIVA.class);
        when(condicionIVAqueDiscrimina.isDiscriminaIVA()).thenReturn(Boolean.TRUE);
        CondicionIVA condicionIVAqueNoDiscrimina = Mockito.mock(CondicionIVA.class);
        when(condicionIVAqueNoDiscrimina.isDiscriminaIVA()).thenReturn(Boolean.FALSE);
        when(empresa.getCondicionIVA()).thenReturn(condicionIVAqueDiscrimina);
        when(proveedor.getCondicionIVA()).thenReturn(condicionIVAqueNoDiscrimina);
        TipoDeComprobante[] expResult = new TipoDeComprobante[3];
        expResult[0] = TipoDeComprobante.FACTURA_C;
        expResult[1] = TipoDeComprobante.FACTURA_X;
        expResult[2] = TipoDeComprobante.PRESUPUESTO;
        TipoDeComprobante[] result = facturaService.getTipoFacturaCompra(empresa, proveedor);
        assertArrayEquals(expResult, result);
    }

    @Test
    public void shouldGetTipoFacturaCompraWhenEmpresaNoDiscriminaIVAYProveedorSI() {
        Empresa empresa = Mockito.mock(Empresa.class);
        Proveedor proveedor = Mockito.mock(Proveedor.class);
        CondicionIVA condicionIVAqueDiscrimina = Mockito.mock(CondicionIVA.class);
        when(condicionIVAqueDiscrimina.isDiscriminaIVA()).thenReturn(Boolean.TRUE);
        CondicionIVA condicionIVAqueNoDiscrimina = Mockito.mock(CondicionIVA.class);
        when(condicionIVAqueNoDiscrimina.isDiscriminaIVA()).thenReturn(Boolean.FALSE);
        when(empresa.getCondicionIVA()).thenReturn(condicionIVAqueNoDiscrimina);
        when(proveedor.getCondicionIVA()).thenReturn(condicionIVAqueDiscrimina);
        TipoDeComprobante[] expResult = new TipoDeComprobante[3];
        expResult[0] = TipoDeComprobante.FACTURA_B;
        expResult[1] = TipoDeComprobante.FACTURA_X;
        expResult[2] = TipoDeComprobante.PRESUPUESTO;
        empresa.getCondicionIVA().isDiscriminaIVA();
        TipoDeComprobante[] result = facturaService.getTipoFacturaCompra(empresa, proveedor);
        assertArrayEquals(expResult, result);
    }

    @Test
    public void shouldGetTipoFacturaCompraWhenEmpresaNoDiscriminaYProveedorTampoco() {
        Empresa empresa = Mockito.mock(Empresa.class);
        Proveedor proveedor = Mockito.mock(Proveedor.class);
        CondicionIVA condicionIVAqueNoDiscrimina = Mockito.mock(CondicionIVA.class);
        when(condicionIVAqueNoDiscrimina.isDiscriminaIVA()).thenReturn(Boolean.FALSE);
        when(empresa.getCondicionIVA()).thenReturn(condicionIVAqueNoDiscrimina);
        when(proveedor.getCondicionIVA()).thenReturn(condicionIVAqueNoDiscrimina);
        TipoDeComprobante[] expResult = new TipoDeComprobante[3];
        expResult[0] = TipoDeComprobante.FACTURA_C;
        expResult[1] = TipoDeComprobante.FACTURA_X;
        expResult[2] = TipoDeComprobante.PRESUPUESTO;
        empresa.getCondicionIVA().isDiscriminaIVA();
        TipoDeComprobante[] result = facturaService.getTipoFacturaCompra(empresa, proveedor);
        assertArrayEquals(expResult, result);
    }

    @Test
    public void shouldGetTipoFacturaVentaWhenEmpresaDiscriminaYClienteTambien() {
        Empresa empresa = Mockito.mock(Empresa.class);
        Cliente cliente = Mockito.mock(Cliente.class);
        CondicionIVA condicionIVAqueDiscrimina = Mockito.mock(CondicionIVA.class);
        when(condicionIVAqueDiscrimina.isDiscriminaIVA()).thenReturn(Boolean.TRUE);
        when(empresa.getCondicionIVA()).thenReturn(condicionIVAqueDiscrimina);
        when(cliente.getCondicionIVA()).thenReturn(condicionIVAqueDiscrimina);
        TipoDeComprobante[] expResult = {TipoDeComprobante.FACTURA_A, TipoDeComprobante.FACTURA_X, TipoDeComprobante.FACTURA_Y, TipoDeComprobante.PRESUPUESTO};
        TipoDeComprobante[] result = facturaService.getTipoFacturaVenta(empresa, cliente);
        assertArrayEquals(expResult, result);
    }

    @Test
    public void shouldGetTipoFacturaVentaWhenEmpresaDiscriminaYClienteNo() {
        Empresa empresa = Mockito.mock(Empresa.class);
        Cliente cliente = Mockito.mock(Cliente.class);
        CondicionIVA condicionIVAqueDiscrimina = Mockito.mock(CondicionIVA.class);
        CondicionIVA condicionIVAqueNoDiscrimina = Mockito.mock(CondicionIVA.class);
        when(condicionIVAqueDiscrimina.isDiscriminaIVA()).thenReturn(Boolean.TRUE);
        when(condicionIVAqueNoDiscrimina.isDiscriminaIVA()).thenReturn(Boolean.FALSE);
        when(empresa.getCondicionIVA()).thenReturn(condicionIVAqueDiscrimina);
        when(cliente.getCondicionIVA()).thenReturn(condicionIVAqueNoDiscrimina);
        TipoDeComprobante[] expResult = {TipoDeComprobante.FACTURA_B, TipoDeComprobante.FACTURA_X, TipoDeComprobante.FACTURA_Y, TipoDeComprobante.PRESUPUESTO};
        TipoDeComprobante[] result = facturaService.getTipoFacturaVenta(empresa, cliente);
        assertArrayEquals(expResult, result);
    }

    @Test
    public void shouldGetTipoFacturaVentaWhenEmpresaNoDiscriminaYClienteSi() {
        Empresa empresa = Mockito.mock(Empresa.class);
        Cliente cliente = Mockito.mock(Cliente.class);
        CondicionIVA condicionIVAqueDiscrimina = Mockito.mock(CondicionIVA.class);
        CondicionIVA condicionIVAqueNoDiscrimina = Mockito.mock(CondicionIVA.class);
        when(condicionIVAqueDiscrimina.isDiscriminaIVA()).thenReturn(Boolean.TRUE);
        when(condicionIVAqueNoDiscrimina.isDiscriminaIVA()).thenReturn(Boolean.FALSE);
        when(empresa.getCondicionIVA()).thenReturn(condicionIVAqueNoDiscrimina);
        when(cliente.getCondicionIVA()).thenReturn(condicionIVAqueDiscrimina);
        TipoDeComprobante[] expResult = {TipoDeComprobante.FACTURA_C, TipoDeComprobante.FACTURA_X, TipoDeComprobante.FACTURA_Y, TipoDeComprobante.PRESUPUESTO};
        TipoDeComprobante[] result = facturaService.getTipoFacturaVenta(empresa, cliente);
        assertArrayEquals(expResult, result);
    }

    @Test
    public void shouldGetTipoFacturaVentaWhenEmpresaNoDiscriminaIVAYClienteNO() {
        Empresa empresa = Mockito.mock(Empresa.class);
        Cliente cliente = Mockito.mock(Cliente.class);
        CondicionIVA condicionIVAqueNoDiscrimina = Mockito.mock(CondicionIVA.class);
        when(condicionIVAqueNoDiscrimina.isDiscriminaIVA()).thenReturn(Boolean.TRUE);
        when(condicionIVAqueNoDiscrimina.isDiscriminaIVA()).thenReturn(Boolean.FALSE);
        when(empresa.getCondicionIVA()).thenReturn(condicionIVAqueNoDiscrimina);
        when(cliente.getCondicionIVA()).thenReturn(condicionIVAqueNoDiscrimina);
        TipoDeComprobante[] expResult = {TipoDeComprobante.FACTURA_C, TipoDeComprobante.FACTURA_X, TipoDeComprobante.FACTURA_Y, TipoDeComprobante.PRESUPUESTO};
        TipoDeComprobante[] result = facturaService.getTipoFacturaVenta(empresa, cliente);
        assertArrayEquals(expResult, result);
    }

    @Test
    public void shouldGetTiposFacturaWhenEmpresaDiscriminaIVA() {
        Empresa empresa = Mockito.mock(Empresa.class);
        CondicionIVA condicionIVAqueDiscrimina = Mockito.mock(CondicionIVA.class);
        when(condicionIVAqueDiscrimina.isDiscriminaIVA()).thenReturn(Boolean.TRUE);
        when(empresa.getCondicionIVA()).thenReturn(condicionIVAqueDiscrimina);
        TipoDeComprobante[] expResult = {TipoDeComprobante.FACTURA_A, TipoDeComprobante.FACTURA_B, TipoDeComprobante.FACTURA_X, TipoDeComprobante.FACTURA_Y, TipoDeComprobante.PRESUPUESTO};
        TipoDeComprobante[] result = facturaService.getTiposFacturaSegunEmpresa(empresa);
        assertArrayEquals(expResult, result);
    }

    @Test
    public void shouldGetTiposFacturaWhenEmpresaNoDiscriminaIVA() {
        Empresa empresa = Mockito.mock(Empresa.class);
        CondicionIVA condicionIVAqueNoDiscrimina = Mockito.mock(CondicionIVA.class);
        when(condicionIVAqueNoDiscrimina.isDiscriminaIVA()).thenReturn(Boolean.FALSE);
        when(empresa.getCondicionIVA()).thenReturn(condicionIVAqueNoDiscrimina);
        TipoDeComprobante[] expResult = {TipoDeComprobante.FACTURA_C, TipoDeComprobante.FACTURA_X, TipoDeComprobante.FACTURA_Y, TipoDeComprobante.PRESUPUESTO};
        TipoDeComprobante[] result = facturaService.getTiposFacturaSegunEmpresa(empresa);
        assertArrayEquals(expResult, result);
    }

    @Test
    public void shouldDividirFactura() {
        when(facturaVentaRepository.buscarMayorNumFacturaSegunTipo(TipoDeComprobante.FACTURA_X, 1L, new EmpresaBuilder().build().getId_Empresa())).thenReturn(1L);
        when(facturaVentaRepository.buscarMayorNumFacturaSegunTipo(TipoDeComprobante.FACTURA_A, 1L, new EmpresaBuilder().build().getId_Empresa())).thenReturn(1L);
        RenglonFactura renglon1 = Mockito.mock(RenglonFactura.class);
        RenglonFactura renglon2 = Mockito.mock(RenglonFactura.class);
        Producto producto = Mockito.mock(Producto.class);
        when(producto.getId_Producto()).thenReturn(1L);
        when(producto.getCodigo()).thenReturn("1");
        when(producto.getDescripcion()).thenReturn("producto test");
        Medida medida = Mockito.mock(Medida.class);
        when(producto.getMedida()).thenReturn(medida);
        when(producto.getPrecioVentaPublico()).thenReturn(1.0);
        when(producto.getIva_porcentaje()).thenReturn(21.00);
        when(producto.getImpuestoInterno_porcentaje()).thenReturn(0.0);
        when(producto.getPrecioLista()).thenReturn(1.0);
        when(productoService.getProductoPorId(1L)).thenReturn(producto);
        when(renglon1.getId_ProductoItem()).thenReturn(1L);
        when(renglon2.getId_ProductoItem()).thenReturn(1L);
        when(renglon1.getCantidad()).thenReturn(4.00);
        when(renglon2.getCantidad()).thenReturn(7.00);
        List<RenglonFactura> renglones = new ArrayList<>();
        renglones.add(renglon1);
        renglones.add(renglon2);
        FacturaVenta factura = new FacturaVenta();
        factura.setRenglones(renglones);
        factura.setFecha(new Date());
        factura.setTransportista(new TransportistaBuilder().build());
        factura.setEmpresa(new EmpresaBuilder().build());
        factura.setCliente(new ClienteBuilder().build());
        Usuario usuario = new Usuario();
        usuario.setNombre("Marian Jhons  help");
        factura.setUsuario(usuario);
        factura.setTipoComprobante(TipoDeComprobante.FACTURA_A);
        int[] indices = {0, 1};
        int cantidadDeFacturasEsperadas = 2;
        List<Factura> result = facturaService.dividirFactura(factura, indices);
        double cantidadRenglon1PrimeraFactura = result.get(0).getRenglones().get(0).getCantidad();
        double cantidadRenglon2PrimeraFactura = result.get(0).getRenglones().get(1).getCantidad();
        double cantidadRenglon1SegundaFactura = result.get(1).getRenglones().get(0).getCantidad();
        double cantidadRenglon2SegundaFactura = result.get(1).getRenglones().get(1).getCantidad();
        assertEquals(cantidadDeFacturasEsperadas, result.size());
        assertEquals(2, cantidadRenglon1SegundaFactura, 0); 
        assertEquals(4, cantidadRenglon2SegundaFactura, 0); 
        assertEquals(2, cantidadRenglon1PrimeraFactura, 0);
        assertEquals(3, cantidadRenglon2PrimeraFactura, 0);
    }

    //Calculos
    @Test
    public void shouldCalcularSubTotal() {
        RenglonFactura renglon1 = new RenglonFactura();
        renglon1.setImporte(5.601);
        RenglonFactura renglon2 = new RenglonFactura();
        renglon2.setImporte(18.052);
        RenglonFactura renglon3 = new RenglonFactura();
        renglon3.setImporte(10.011);
        List<RenglonFactura> renglones = new ArrayList<>();
        renglones.add(renglon1);
        renglones.add(renglon2);
        renglones.add(renglon3);
        BigDecimal[] importes = new BigDecimal[renglones.size()];
        int indice = 0;
        for(RenglonFactura renglon : renglones) {
            importes[indice] = new BigDecimal(renglon.getImporte());
            indice++;
        }
        double resultadoEsperado = 33.664;
        BigDecimal resultadoObtenido = facturaService.calcularSubTotal(importes);
        assertEquals(resultadoEsperado, resultadoObtenido.doubleValue(), 0);
    }

    @Test
    public void shouldCacularDescuentoNeto() {
        Double resultadoEsperado = 11.773464749999999;
        BigDecimal resultadoObtenido = facturaService.calcularDescuentoNeto(new BigDecimal(78.255), new BigDecimal(15.045));
        assertEquals(resultadoEsperado, resultadoObtenido.doubleValue(), 0);
    }

    @Test
    public void shouldCalcularRecargoNeto() {
        double resultadoEsperado = 12.11047244;
        BigDecimal resultadoObtenido = facturaService.calcularRecargoNeto(new BigDecimal(78.122), new BigDecimal(15.502));
        assertEquals(resultadoEsperado, resultadoObtenido.doubleValue(), 0);
    }

    @Test
    public void shouldCalcularSubTotalBrutoFacturaA() {
        double resultadoEsperado = 220.477;
        BigDecimal resultadoObtenido = facturaService.calcularSubTotalBruto(TipoDeComprobante.FACTURA_A, new BigDecimal(225.025), new BigDecimal(10.454), new BigDecimal(15.002), BigDecimal.ZERO, BigDecimal.ZERO);
        assertEquals(resultadoEsperado, resultadoObtenido.doubleValue(), 0);
    }
    
    @Test
    public void shouldCalcularSubTotalBrutoFacturaB() {
        double resultadoEsperado = 795.2175;
        BigDecimal resultadoObtenido = facturaService.calcularSubTotalBruto(TipoDeComprobante.FACTURA_B, new BigDecimal(1205.5), new BigDecimal(80.5), new BigDecimal(111.05), new BigDecimal(253.155), new BigDecimal(126.5775));
        assertEquals(resultadoEsperado, resultadoObtenido.doubleValue(), 0);
    }

    @Test
    public void shouldCalcularIva_netoWhenLaFacturaEsA() {
        RenglonFactura renglon1 = new RenglonFactura();
        renglon1.setCantidad(12);
        renglon1.setIva_porcentaje(21);
        renglon1.setIva_neto(125.5);
        RenglonFactura renglon2 = new RenglonFactura();
        renglon2.setCantidad(8);
        renglon2.setIva_porcentaje(21);
        renglon2.setIva_neto(240.2);
        RenglonFactura renglon3 = new RenglonFactura();
        renglon3.setCantidad(4);
        renglon3.setIva_porcentaje(10.5);
        renglon3.setIva_neto(110.5);
        List<RenglonFactura> renglones = new ArrayList<>();
        renglones.add(renglon1);
        renglones.add(renglon2);
        renglones.add(renglon3);
        //El renglon3 no lo deberia tener en cuenta para el calculo ya que NO es 21% de IVA
        double resultadoEsperado = 3427.6;
        int size = renglones.size();
        BigDecimal[] cantidades = new BigDecimal[size];        
        BigDecimal[] ivaPorcentajes = new BigDecimal[size];
        BigDecimal[] ivaNetos = new BigDecimal[size];
        int i = 0;
        for (RenglonFactura r : renglones) {
            cantidades[i] = new BigDecimal(r.getCantidad());            
            ivaPorcentajes[i] = new BigDecimal(r.getIva_porcentaje());
            ivaNetos[i] = new BigDecimal(r.getIva_neto());
            i++;
        }       
        BigDecimal resultadoObtenido = facturaService.calcularIvaNetoFactura(TipoDeComprobante.FACTURA_A,
                cantidades, ivaPorcentajes, ivaNetos, new BigDecimal(21), BigDecimal.ZERO, BigDecimal.ZERO);
        assertEquals(resultadoEsperado, resultadoObtenido.doubleValue(), 0);
    }

    @Test
    public void shouldCalcularIva_netoWhenLaFacturaEsX() {
        RenglonFactura renglon1 = new RenglonFactura();
        renglon1.setImporte(5.601);
        renglon1.setIva_porcentaje(21);
        RenglonFactura renglon2 = new RenglonFactura();
        renglon2.setImporte(18.052);
        renglon2.setIva_porcentaje(21);
        RenglonFactura renglon3 = new RenglonFactura();
        renglon3.setImporte(10.011);
        renglon3.setIva_porcentaje(10.5);
        List<RenglonFactura> renglones = new ArrayList<>();
        renglones.add(renglon1);
        renglones.add(renglon2);
        renglones.add(renglon3);
        double resultadoEsperado = 0;
        int size = renglones.size();
        BigDecimal[] cantidades = new BigDecimal[size];        
        BigDecimal[] ivaPorcentajes = new BigDecimal[size];
        BigDecimal[] ivaNetos = new BigDecimal[size];
        int i = 0;
        for (RenglonFactura r : renglones) {
            cantidades[i] = new BigDecimal(r.getCantidad());            
            ivaPorcentajes[i] = new BigDecimal(r.getIva_porcentaje());
            ivaNetos[i] = new BigDecimal(r.getIva_neto());
            i++;
        } 
        BigDecimal resultadoObtenido = facturaService.calcularIvaNetoFactura(TipoDeComprobante.FACTURA_X, 
                cantidades, ivaPorcentajes, ivaNetos, new BigDecimal(21), BigDecimal.ZERO, BigDecimal.ZERO);
        assertEquals(resultadoEsperado, resultadoObtenido.doubleValue(), 0);
    }

    @Test
    public void shouldCalcularImpInterno_neto() {
        RenglonFactura renglon1 = new RenglonFactura();
        renglon1.setImporte(5.601);
        renglon1.setImpuesto_porcentaje(15.304);
        RenglonFactura renglon2 = new RenglonFactura();
        renglon2.setImporte(18.052);
        renglon2.setImpuesto_porcentaje(9.043);
        RenglonFactura renglon3 = new RenglonFactura();
        renglon3.setImporte(10.011);
        renglon3.setImpuesto_porcentaje(4.502);
        List<RenglonFactura> renglones = new ArrayList<>();
        renglones.add(renglon1);
        renglones.add(renglon2);
        renglones.add(renglon3);
        BigDecimal[] importes = new BigDecimal[renglones.size()];
        BigDecimal[] impuestoPorcentajes = new BigDecimal[renglones.size()];
        int indice = 0;
        for(RenglonFactura renglon : renglones) {
            importes[indice] = new BigDecimal(renglon.getImporte());
            impuestoPorcentajes[indice] = new BigDecimal(renglon.getImpuesto_porcentaje());
            indice++;
        }
        double resultadoEsperado = 3.3197328185647996;
        BigDecimal resultadoObtenido = facturaService.calcularImpInternoNeto(TipoDeComprobante.FACTURA_A, new BigDecimal(9.104), new BigDecimal(22.008), importes, impuestoPorcentajes);
        assertEquals(resultadoEsperado, resultadoObtenido.doubleValue(), 0);
    }

    @Test
    public void shouldCalcularTotal() {
        double resultadoEsperado = 386.363;
        BigDecimal resultadoObtenido = facturaService.calcularTotal(new BigDecimal(350.451), new BigDecimal(10.753), new BigDecimal(25.159));
        assertEquals(resultadoEsperado, resultadoObtenido.doubleValue(), 0);
    }

    @Test
    public void shouldCalcularImporte() {
        double resultadoEsperado = 90;
        double cantidad = 10;
        double precioUnitario = 10;
        double descuento = 1;
        BigDecimal resultadoObtenido = facturaService.calcularImporte(new BigDecimal(cantidad), new BigDecimal(precioUnitario), new BigDecimal(descuento));
        assertEquals(resultadoEsperado, resultadoObtenido.doubleValue(), 0);
    }

    @Test
    public void shouldCalcularIVANetoWhenCompraConFacturaA() {        
        Producto producto = new Producto();
        producto.setPrecioCosto(100);
        producto.setPrecioVentaPublico(121);
        producto.setIva_porcentaje(21);
        double resultadoEsperado = 21;
        BigDecimal resultadoObtenido = facturaService.calcularIVANetoRenglon(Movimiento.COMPRA, TipoDeComprobante.FACTURA_A, producto, BigDecimal.ZERO);
        assertEquals(resultadoEsperado, resultadoObtenido.doubleValue(), 0);
    }
    
    @Test
    public void shouldCalcularIVANetoWhenCompraConFacturaB() {
        Producto producto = new Producto();
        producto.setPrecioCosto(200);
        producto.setPrecioVentaPublico(1000);
        producto.setIva_porcentaje(21);
        double resultadoEsperado = 42;
        BigDecimal resultadoObtenido = facturaService.calcularIVANetoRenglon(Movimiento.COMPRA, TipoDeComprobante.FACTURA_B, producto, BigDecimal.ZERO);
        assertEquals(resultadoEsperado, resultadoObtenido.doubleValue(), 0);
    }

    @Test
    public void shouldCalcularIVANetoWhenVentaConFacturaA() {
        Producto producto = new Producto();
        producto.setPrecioVentaPublico(121);
        producto.setIva_porcentaje(21);
        double resultadoEsperado = 25.41;
        BigDecimal resultadoObtenido = facturaService.calcularIVANetoRenglon(Movimiento.VENTA, TipoDeComprobante.FACTURA_A, producto, BigDecimal.ZERO);
        assertEquals(resultadoEsperado, resultadoObtenido.doubleValue(), 0);
    }
    
    public void shouldCalcularIVANetoWhenVentaConFacturaB() {
        Producto producto = new Producto();
        producto.setPrecioVentaPublico(1000);
        producto.setIva_porcentaje(21);
        double resultadoEsperado = 210;
        BigDecimal resultadoObtenido = facturaService.calcularIVANetoRenglon(Movimiento.VENTA, TipoDeComprobante.FACTURA_B, producto, BigDecimal.ZERO);
        assertEquals(resultadoEsperado, resultadoObtenido.doubleValue(), 0);
    }

    @Test
    public void shouldCalcularPrecioUnitarioWhenEsUnaVentaConFacturaA() {
        Producto producto = new Producto();
        producto.setPrecioCosto(100);
        producto.setPrecioVentaPublico(121);
        producto.setIva_porcentaje(21);
        double resultadoEsperado = 121;
        BigDecimal resultadoObtenido = facturaService.calcularPrecioUnitario(Movimiento.VENTA, TipoDeComprobante.FACTURA_A, producto);
        assertEquals(resultadoEsperado, resultadoObtenido.doubleValue(), 0);
    }

    @Test
    public void shouldCalcularPrecioUnitarioWhenEsUnaVentaConFacturaX() {
        Producto producto = new Producto();
        producto.setPrecioCosto(100);
        producto.setPrecioVentaPublico(121);
        producto.setIva_porcentaje(21);
        double resultadoEsperado = 121;
        BigDecimal resultadoObtenido = facturaService.calcularPrecioUnitario(Movimiento.VENTA, TipoDeComprobante.FACTURA_X, producto);
        assertEquals(resultadoEsperado, resultadoObtenido.doubleValue(), 0);
    }

    @Test
    public void shouldCalcularPrecioUnitarioWhenEsUnaCompraConFacturaA() {
        Producto producto = new Producto();
        producto.setPrecioCosto(100);
        producto.setPrecioVentaPublico(121);
        producto.setIva_porcentaje(21);
        double resultadoEsperado = 100;
        BigDecimal resultadoObtenido = facturaService.calcularPrecioUnitario(Movimiento.COMPRA, TipoDeComprobante.FACTURA_A, producto);
        assertEquals(resultadoEsperado, resultadoObtenido.doubleValue(), 0);
    }

    @Test
    public void shouldCalcularPrecioUnitarioWhenEsUnaCompraConFacturaX() {
        Producto producto = new Producto();
        producto.setPrecioCosto(100);
        producto.setPrecioVentaPublico(121);
        producto.setIva_porcentaje(21);
        double resultadoEsperado = 100;
        BigDecimal resultadoObtenido = facturaService.calcularPrecioUnitario(Movimiento.COMPRA, TipoDeComprobante.FACTURA_X, producto);
        assertEquals(resultadoEsperado, resultadoObtenido.doubleValue(), 0);
    }

    @Test
    public void shouldCalcularPrecioUnitarioWhenEsUnaCompraConFacturaB() {
        Producto producto = new Producto();
        producto.setPrecioCosto(100);
        producto.setPrecioVentaPublico(121);
        producto.setIva_porcentaje(21);
        double resultadoEsperado = 121;
        BigDecimal resultadoObtenido = facturaService.calcularPrecioUnitario(Movimiento.COMPRA, TipoDeComprobante.FACTURA_B, producto);
        assertEquals(resultadoEsperado, resultadoObtenido.doubleValue(), 0);
    }

    @Test
    public void shouldCalcularPrecioUnitarioWhenEsUnaCompraConFacturaC() {
        Producto producto = new Producto();
        producto.setPrecioCosto(100);
        producto.setPrecioVentaPublico(121);
        producto.setIva_porcentaje(21);
        double resultadoEsperado = 121;
        double resultadoObtenido = facturaService.calcularPrecioUnitario(Movimiento.COMPRA, TipoDeComprobante.FACTURA_C, producto).doubleValue();
        assertEquals(resultadoEsperado, resultadoObtenido, 0);
    }

    @Test
    public void shouldCalcularPrecioUnitarioWhenEsUnaCompraConFacturaY() {
        Producto producto = new Producto();
        producto.setPrecioCosto(100);
        producto.setPrecioVentaPublico(121);
        producto.setIva_porcentaje(21);
        double resultadoEsperado = 121;
        double resultadoObtenido = facturaService.calcularPrecioUnitario(Movimiento.COMPRA, TipoDeComprobante.FACTURA_Y, producto).doubleValue();
        assertEquals(resultadoEsperado, resultadoObtenido, 0);
    }

    @Test
    public void shouldCalcularPrecioUnitarioWhenEsUnaVentaConFacturaB() {
        Producto producto = new Producto();
        producto.setPrecioCosto(100);
        producto.setGanancia_neto(100);
        producto.setIva_porcentaje(21);
        producto.setIva_neto(42);
        producto.setPrecioVentaPublico(200);
        producto.setPrecioLista(242);        
        double resultadoEsperado = 242;
        double resultadoObtenido = facturaService.calcularPrecioUnitario(Movimiento.VENTA, TipoDeComprobante.FACTURA_B, producto).doubleValue();
        assertEquals(resultadoEsperado, resultadoObtenido, 0);
    }

    @Test
    public void shouldCalcularPrecioUnitarioWhenEsUnaVentaConFacturaC() {
        Producto producto = new Producto();
        producto.setPrecioCosto(100);
        producto.setGanancia_neto(100);
        producto.setIva_porcentaje(21);
        producto.setIva_neto(42);
        producto.setPrecioVentaPublico(200);
        producto.setPrecioLista(242);
        double resultadoEsperado = 242;
        double resultadoObtenido = facturaService.calcularPrecioUnitario(Movimiento.VENTA, TipoDeComprobante.FACTURA_C, producto).doubleValue();
        assertEquals(resultadoEsperado, resultadoObtenido, 0);
    }

    @Test
    public void shouldCalcularPrecioUnitarioWhenVentaYFacturaY() {
        Producto producto = new Producto();
        producto.setPrecioCosto(100);
        producto.setGanancia_neto(100);
        producto.setIva_porcentaje(21);
        producto.setIva_neto(42);
        producto.setPrecioVentaPublico(200);
        producto.setPrecioLista(242);
        double resultadoEsperado = 221;
        double resultadoObtenido = facturaService.calcularPrecioUnitario(Movimiento.VENTA, TipoDeComprobante.FACTURA_Y, producto).doubleValue();
        assertEquals(resultadoEsperado, resultadoObtenido, 0);
    }
}
