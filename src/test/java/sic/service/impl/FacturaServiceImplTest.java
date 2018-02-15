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
        RenglonFactura renglon3 = Mockito.mock(RenglonFactura.class);
        RenglonFactura renglon4 = Mockito.mock(RenglonFactura.class);
        RenglonFactura renglon5 = Mockito.mock(RenglonFactura.class);
        Producto producto = Mockito.mock(Producto.class);
        when(producto.getId_Producto()).thenReturn(1L);
        when(producto.getCodigo()).thenReturn("1");
        when(producto.getDescripcion()).thenReturn("producto test");
        Medida medida = Mockito.mock(Medida.class);
        when(producto.getMedida()).thenReturn(medida);
        when(producto.getPrecioVentaPublico()).thenReturn(BigDecimal.ONE);
        when(producto.getIva_porcentaje()).thenReturn(new BigDecimal("21.00"));
        when(producto.getImpuestoInterno_porcentaje()).thenReturn(BigDecimal.ZERO);
        when(producto.getPrecioLista()).thenReturn(BigDecimal.ONE);
        when(productoService.getProductoPorId(1L)).thenReturn(producto);
        when(renglon1.getId_ProductoItem()).thenReturn(1L);
        when(renglon1.getIva_neto()).thenReturn(new BigDecimal("21"));
        when(renglon1.getDescuento_porcentaje()).thenReturn(BigDecimal.ZERO);
        when(renglon1.getCantidad()).thenReturn(new BigDecimal("4.00"));
        when(renglon2.getId_ProductoItem()).thenReturn(1L);
        when(renglon2.getIva_neto()).thenReturn(new BigDecimal("10.5"));
        when(renglon2.getDescuento_porcentaje()).thenReturn(BigDecimal.ZERO);
        when(renglon2.getCantidad()).thenReturn(new BigDecimal("7.00"));
        when(renglon3.getId_ProductoItem()).thenReturn(1L);
        when(renglon3.getIva_neto()).thenReturn(new BigDecimal("21"));
        when(renglon3.getDescuento_porcentaje()).thenReturn(BigDecimal.ZERO);
        when(renglon3.getCantidad()).thenReturn(new BigDecimal("12.8"));
        when(renglon4.getId_ProductoItem()).thenReturn(1L);
        when(renglon4.getIva_neto()).thenReturn(new BigDecimal("21"));
        when(renglon4.getDescuento_porcentaje()).thenReturn(BigDecimal.ZERO);
        when(renglon4.getCantidad()).thenReturn(new BigDecimal("1.2"));
        when(renglon5.getId_ProductoItem()).thenReturn(1L);
        when(renglon5.getIva_neto()).thenReturn(new BigDecimal("21"));
        when(renglon5.getDescuento_porcentaje()).thenReturn(BigDecimal.ZERO);
        when(renglon5.getCantidad()).thenReturn(new BigDecimal("0.8"));
        List<RenglonFactura> renglones = new ArrayList<>();
        renglones.add(renglon1);
        renglones.add(renglon2);
        renglones.add(renglon3);
        renglones.add(renglon4);
        renglones.add(renglon5);
        FacturaVenta factura = new FacturaVenta();
        factura.setDescuento_porcentaje(BigDecimal.ZERO);
        factura.setRecargo_porcentaje(BigDecimal.ZERO);
        factura.setRenglones(renglones);
        factura.setFecha(new Date());
        factura.setTransportista(new TransportistaBuilder().build());
        factura.setEmpresa(new EmpresaBuilder().build());
        factura.setCliente(new ClienteBuilder().build());
        Usuario usuario = new Usuario();
        usuario.setNombre("Marian Jhons  help");
        factura.setUsuario(usuario);
        factura.setTipoComprobante(TipoDeComprobante.FACTURA_A);
        int[] indices = {0, 1, 2, 3};
        int cantidadDeFacturasEsperadas = 2;
        int cantidadDeRenglonesEsperadosFX = 4;
        int cantidadDeRenglonesEsperadosFA= 5;
        List<Factura> result = facturaService.dividirFactura(factura, indices);
        assertEquals(cantidadDeFacturasEsperadas, result.size());
        assertEquals(cantidadDeRenglonesEsperadosFX, result.get(0).getRenglones().size());
        assertEquals(cantidadDeRenglonesEsperadosFA, result.get(1).getRenglones().size());
        BigDecimal cantidadPrimerRenglonFacturaX = result.get(0).getRenglones().get(0).getCantidad();
        BigDecimal cantidadSegundoRenglonFacturaX = result.get(0).getRenglones().get(1).getCantidad();
        BigDecimal cantidadTercerRenglonFacturaX = result.get(0).getRenglones().get(2).getCantidad();
        BigDecimal cantidadCuartoRenglonFacturaX = result.get(0).getRenglones().get(3).getCantidad();       
        BigDecimal cantidadPrimerRenglonFacturaA = result.get(1).getRenglones().get(0).getCantidad();
        BigDecimal cantidadSegundoRenglonFacturaA = result.get(1).getRenglones().get(1).getCantidad();
        BigDecimal cantidadTercerRenglonFacturaA = result.get(1).getRenglones().get(2).getCantidad();
        BigDecimal cantidadCuartoRenglonFacturaA = result.get(1).getRenglones().get(3).getCantidad();
        BigDecimal cantidadQuintoRenglonFacturaA = result.get(1).getRenglones().get(4).getCantidad();
        assertTrue("Las cantidades no son las esperadas", cantidadPrimerRenglonFacturaA.compareTo(new BigDecimal("2")) == 0);
        assertTrue("Las cantidades no son las esperadas", cantidadSegundoRenglonFacturaA.compareTo(new BigDecimal("4")) == 0);
        assertTrue("Las cantidades no son las esperadas", cantidadTercerRenglonFacturaA.compareTo(new BigDecimal("6.4")) == 0);
        assertTrue("Las cantidades no son las esperadas", cantidadCuartoRenglonFacturaA.compareTo(new BigDecimal("0.6")) == 0);
        assertTrue("Las cantidades no son las esperadas", cantidadQuintoRenglonFacturaA.compareTo(new BigDecimal("0.8")) == 0);
        assertTrue("Las cantidades no son las esperadas", cantidadPrimerRenglonFacturaX.compareTo(new BigDecimal("2")) == 0);
        assertTrue("Las cantidades no son las esperadas", cantidadSegundoRenglonFacturaX.compareTo(new BigDecimal("3")) == 0);
        assertTrue("Las cantidades no son las esperadas", cantidadTercerRenglonFacturaX.compareTo(new BigDecimal("6.4")) == 0);
        assertTrue("Las cantidades no son las esperadas", cantidadCuartoRenglonFacturaX.compareTo(new BigDecimal("0.6")) == 0);
    }

    //Calculos
    @Test
    public void shouldCalcularSubTotal() {
        RenglonFactura renglon1 = new RenglonFactura();
        renglon1.setImporte(new BigDecimal("5.601"));
        RenglonFactura renglon2 = new RenglonFactura();
        renglon2.setImporte(new BigDecimal("18.052"));
        RenglonFactura renglon3 = new RenglonFactura();
        renglon3.setImporte(new BigDecimal("10.011"));
        List<RenglonFactura> renglones = new ArrayList<>();
        renglones.add(renglon1);
        renglones.add(renglon2);
        renglones.add(renglon3);
        BigDecimal[] importes = new BigDecimal[renglones.size()];
        int indice = 0;
        for(RenglonFactura renglon : renglones) {
            importes[indice] = renglon.getImporte();
            indice++;
        }
        assertTrue("El subtotal no es el esperado", facturaService.calcularSubTotal(importes)
                .doubleValue() == 33.664);
    }

    @Test
    public void shouldCacularDescuentoNeto() {
        assertTrue("El descuento neto no es el esperado", facturaService.calcularDescuentoNeto(new BigDecimal("78.255"), new BigDecimal("15.045"))
                .doubleValue() == 11.773464750000000);
    }

    @Test
    public void shouldCalcularRecargoNeto() {
        assertTrue("El recargo neto no es el esperado", 
                facturaService.calcularRecargoNeto(new BigDecimal("78.122"), new BigDecimal("15.502"))
                .doubleValue() == 12.11047244);
    }

    @Test
    public void shouldCalcularSubTotalBrutoFacturaA() {
        assertTrue("El sub total bruto no es el esperado",
                facturaService.calcularSubTotalBruto(TipoDeComprobante.FACTURA_A, new BigDecimal("225.025"), new BigDecimal("10.454"), new BigDecimal("15.002"), BigDecimal.ZERO, BigDecimal.ZERO)
                        .doubleValue() == 220.477);
    }

    @Test
    public void shouldCalcularSubTotalBrutoFacturaB() {
        assertTrue("El sub total bruto no es el esperado",
                facturaService.calcularSubTotalBruto(TipoDeComprobante.FACTURA_B, new BigDecimal("1205.5"), new BigDecimal("80.5"), new BigDecimal("111.05"), new BigDecimal("253.155"), new BigDecimal("126.5775"))
                        .doubleValue() == 795.2175);
    }

    @Test
    public void shouldCalcularIva_netoWhenLaFacturaEsA() {
        RenglonFactura renglon1 = new RenglonFactura();
        renglon1.setCantidad(new BigDecimal("12"));
        renglon1.setIva_porcentaje(new BigDecimal("21"));
        renglon1.setIva_neto(new BigDecimal("125.5"));
        RenglonFactura renglon2 = new RenglonFactura();
        renglon2.setCantidad(new BigDecimal("8"));
        renglon2.setIva_porcentaje(new BigDecimal("21"));
        renglon2.setIva_neto(new BigDecimal("240.2"));
        RenglonFactura renglon3 = new RenglonFactura();
        renglon3.setCantidad(new BigDecimal("4"));
        renglon3.setIva_porcentaje(new BigDecimal("10.5"));
        renglon3.setIva_neto(new BigDecimal("110.5"));
        List<RenglonFactura> renglones = new ArrayList<>();
        renglones.add(renglon1);
        renglones.add(renglon2);
        renglones.add(renglon3);
        //El renglon3 no lo deberia tener en cuenta para el calculo ya que NO es 21% de IVA
        int size = renglones.size();
        BigDecimal[] cantidades = new BigDecimal[size];        
        BigDecimal[] ivaPorcentajes = new BigDecimal[size];
        BigDecimal[] ivaNetos = new BigDecimal[size];
        int i = 0;
        for (RenglonFactura r : renglones) {
            cantidades[i] = r.getCantidad();            
            ivaPorcentajes[i] = r.getIva_porcentaje();
            ivaNetos[i] = r.getIva_neto();
            i++;
        }       
        assertTrue("El iva neto no es el esperado",
                facturaService.calcularIvaNetoFactura(TipoDeComprobante.FACTURA_A, cantidades, ivaPorcentajes, ivaNetos, new BigDecimal("21"), BigDecimal.ZERO, BigDecimal.ZERO)
                        .compareTo(new BigDecimal("3427.6")) == 0);
    }

    @Test
    public void shouldCalcularIva_netoWhenLaFacturaEsX() {
        RenglonFactura renglon1 = new RenglonFactura();
        renglon1.setImporte(new BigDecimal("5.601"));
        renglon1.setIva_porcentaje(BigDecimal.ZERO);
        renglon1.setCantidad(BigDecimal.ONE);
        renglon1.setIva_neto(new BigDecimal("1.17621"));
        RenglonFactura renglon2 = new RenglonFactura();
        renglon2.setImporte(new BigDecimal("18.052"));
        renglon2.setIva_porcentaje(BigDecimal.ZERO);
        renglon2.setCantidad(BigDecimal.ONE);
        renglon2.setIva_neto(new BigDecimal("3.79092"));       
        RenglonFactura renglon3 = new RenglonFactura();
        renglon3.setImporte(new BigDecimal("10.011"));
        renglon3.setIva_porcentaje(BigDecimal.ZERO);
        renglon3.setCantidad(BigDecimal.ONE);
        renglon3.setIva_neto(new BigDecimal("2.10231"));           
        List<RenglonFactura> renglones = new ArrayList<>();
        renglones.add(renglon1);
        renglones.add(renglon2);
        renglones.add(renglon3);
        int size = renglones.size();
        BigDecimal[] cantidades = new BigDecimal[size];        
        BigDecimal[] ivaPorcentajes = new BigDecimal[size];
        BigDecimal[] ivaNetos = new BigDecimal[size];
        int i = 0;
        for (RenglonFactura r : renglones) {
            cantidades[i] = r.getCantidad();            
            ivaPorcentajes[i] = r.getIva_porcentaje();
            ivaNetos[i] = r.getIva_neto();
            i++;
        } 
        assertTrue("El iva neto no es el esperado",
                facturaService.calcularIvaNetoFactura(TipoDeComprobante.FACTURA_X, cantidades, ivaPorcentajes, ivaNetos, new BigDecimal("21"), BigDecimal.ZERO, BigDecimal.ZERO)
                        .doubleValue() == 0);
    }

    @Test
    public void shouldCalcularImpInterno_neto() {
        RenglonFactura renglon1 = new RenglonFactura();
        renglon1.setImporte(new BigDecimal("5.601"));
        renglon1.setImpuesto_porcentaje(new BigDecimal("15.304"));
        RenglonFactura renglon2 = new RenglonFactura();
        renglon2.setImporte(new BigDecimal("18.052"));
        renglon2.setImpuesto_porcentaje(new BigDecimal("9.043"));
        RenglonFactura renglon3 = new RenglonFactura();
        renglon3.setImporte(new BigDecimal("10.011"));
        renglon3.setImpuesto_porcentaje(new BigDecimal("4.502"));
        List<RenglonFactura> renglones = new ArrayList<>();
        renglones.add(renglon1);
        renglones.add(renglon2);
        renglones.add(renglon3);
        BigDecimal[] importes = new BigDecimal[renglones.size()];
        BigDecimal[] impuestoPorcentajes = new BigDecimal[renglones.size()];
        int indice = 0;
        for(RenglonFactura renglon : renglones) {
            importes[indice] = renglon.getImporte();
            impuestoPorcentajes[indice] = renglon.getImpuesto_porcentaje();
            indice++;
        }
        assertTrue("El impuesto interno neto no es el esperado",
                facturaService.calcularImpInternoNeto(TipoDeComprobante.FACTURA_A, new BigDecimal("9.104"), new BigDecimal("22.008"), importes, impuestoPorcentajes)
                        .doubleValue() == 3.3197328185648);
    }

    @Test
    public void shouldCalcularTotal() {
        assertTrue("El total no es el esperado",
                facturaService.calcularTotal(new BigDecimal("350.451"), new BigDecimal("10.753"), new BigDecimal("25.159"))
                        .doubleValue() == 386.363);
    }

    @Test
    public void shouldCalcularImporte() {
        assertTrue("El importe no es el esperado",
                facturaService.calcularImporte(new BigDecimal("10"), new BigDecimal("10"), BigDecimal.ONE)
                        .doubleValue() == 90);
    }

    @Test
    public void shouldCalcularIVANetoWhenCompraConFacturaA() {        
        Producto producto = new Producto();
        producto.setPrecioCosto(new BigDecimal("100"));
        producto.setPrecioVentaPublico(new BigDecimal("121"));
        producto.setIva_porcentaje(new BigDecimal("21"));
        assertTrue("El iva neto no es el esperado",
                facturaService.calcularIVANetoRenglon(Movimiento.COMPRA, TipoDeComprobante.FACTURA_A, producto, BigDecimal.ZERO)
                        .compareTo(new BigDecimal("21")) == 0);
    }
    
    @Test
    public void shouldCalcularIVANetoWhenCompraConFacturaB() {
        Producto producto = new Producto();
        producto.setPrecioCosto(new BigDecimal("200"));
        producto.setPrecioVentaPublico(new BigDecimal("1000"));
        producto.setIva_porcentaje(new BigDecimal("21"));
        assertTrue("El iva neto no es el esperado",
                facturaService.calcularIVANetoRenglon(Movimiento.COMPRA, TipoDeComprobante.FACTURA_B, producto, BigDecimal.ZERO)
                        .doubleValue() == 42);
    }

    @Test
    public void shouldCalcularIVANetoWhenVentaConFacturaA() {
        Producto producto = new Producto();
        producto.setPrecioVentaPublico(new BigDecimal("121"));
        producto.setIva_porcentaje(new BigDecimal("21"));
        assertTrue("El iva neto no es el esperado",
                facturaService.calcularIVANetoRenglon(Movimiento.VENTA, TipoDeComprobante.FACTURA_A, producto, BigDecimal.ZERO)
                        .doubleValue() == 25.41);
    }
    
    public void shouldCalcularIVANetoWhenVentaConFacturaB() {
        Producto producto = new Producto();
        producto.setPrecioVentaPublico(new BigDecimal("1000"));
        producto.setIva_porcentaje(new BigDecimal("21"));
        assertTrue("El iva neto no es el esperado",
                facturaService.calcularIVANetoRenglon(Movimiento.VENTA, TipoDeComprobante.FACTURA_B, producto, BigDecimal.ZERO)
                        .compareTo(new BigDecimal("210")) == 0);
    }

    @Test
    public void shouldCalcularPrecioUnitarioWhenEsUnaVentaConFacturaA() {
        Producto producto = new Producto();
        producto.setPrecioCosto(new BigDecimal("100"));
        producto.setPrecioVentaPublico(new BigDecimal("121"));
        producto.setIva_porcentaje(new BigDecimal("21"));
        assertTrue("El precio unitario no es el esperado",
                facturaService.calcularPrecioUnitario(Movimiento.VENTA, TipoDeComprobante.FACTURA_A, producto)
                        .compareTo(new BigDecimal("121")) == 0);
    }

    @Test
    public void shouldCalcularPrecioUnitarioWhenEsUnaVentaConFacturaX() {
        Producto producto = new Producto();
        producto.setPrecioCosto(new BigDecimal("100"));
        producto.setPrecioVentaPublico(new BigDecimal("121"));
        producto.setIva_porcentaje(new BigDecimal("21"));
        assertTrue("El precio unitario no es el esperado",
                facturaService.calcularPrecioUnitario(Movimiento.VENTA, TipoDeComprobante.FACTURA_X, producto)
                        .compareTo(new BigDecimal("121")) == 0);
    }

    @Test
    public void shouldCalcularPrecioUnitarioWhenEsUnaCompraConFacturaA() {
        Producto producto = new Producto();
        producto.setPrecioCosto(new BigDecimal("100"));
        producto.setPrecioVentaPublico(new BigDecimal("121"));
        producto.setIva_porcentaje(new BigDecimal("21"));
        assertTrue("El precio unitario no es el esperado",
                facturaService.calcularPrecioUnitario(Movimiento.COMPRA, TipoDeComprobante.FACTURA_A, producto)
                        .compareTo(new BigDecimal("100")) == 0);
    }

    @Test
    public void shouldCalcularPrecioUnitarioWhenEsUnaCompraConFacturaX() {
        Producto producto = new Producto();
        producto.setPrecioCosto(new BigDecimal("100"));
        producto.setPrecioVentaPublico(new BigDecimal("121"));
        producto.setIva_porcentaje(new BigDecimal("21"));
        assertTrue("El precio unitario no es el esperado",
                facturaService.calcularPrecioUnitario(Movimiento.COMPRA, TipoDeComprobante.FACTURA_X, producto)
                        .compareTo(new BigDecimal("100")) == 0);
    }

    @Test
    public void shouldCalcularPrecioUnitarioWhenEsUnaCompraConFacturaB() {
        Producto producto = new Producto();
        producto.setPrecioCosto(new BigDecimal("100"));
        producto.setPrecioVentaPublico(new BigDecimal("121"));
        producto.setIva_porcentaje(new BigDecimal("21"));
        producto.setImpuestoInterno_porcentaje(BigDecimal.ZERO);
        assertTrue("El precio unitario no es el esperado",
                facturaService.calcularPrecioUnitario(Movimiento.COMPRA, TipoDeComprobante.FACTURA_B, producto)
                        .compareTo(new BigDecimal("121")) == 0);
    }

    @Test
    public void shouldCalcularPrecioUnitarioWhenEsUnaCompraConFacturaC() {
        Producto producto = new Producto();
        producto.setPrecioCosto(new BigDecimal("100"));
        producto.setPrecioVentaPublico(new BigDecimal("121"));
        producto.setIva_porcentaje(new BigDecimal("21"));
        producto.setImpuestoInterno_porcentaje(BigDecimal.ZERO);
        assertTrue("El precio unitario no es el esperado",
                facturaService.calcularPrecioUnitario(Movimiento.COMPRA, TipoDeComprobante.FACTURA_C, producto)
                        .compareTo(new BigDecimal("121")) == 0);
    }

    @Test
    public void shouldCalcularPrecioUnitarioWhenEsUnaCompraConFacturaY() {
        Producto producto = new Producto();
        producto.setPrecioCosto(new BigDecimal("100"));
        producto.setPrecioVentaPublico(new BigDecimal("121"));
        producto.setIva_porcentaje(new BigDecimal("21"));
        producto.setImpuestoInterno_porcentaje(BigDecimal.ZERO);
        assertTrue("El precio unitario no es el esperado",
                facturaService.calcularPrecioUnitario(Movimiento.COMPRA, TipoDeComprobante.FACTURA_Y, producto)
                        .compareTo(new BigDecimal("121")) == 0);
    }

    @Test
    public void shouldCalcularPrecioUnitarioWhenEsUnaVentaConFacturaB() {
        Producto producto = new Producto();
        producto.setPrecioCosto(new BigDecimal("100"));
        producto.setGanancia_neto(new BigDecimal("100"));
        producto.setIva_porcentaje(new BigDecimal("21"));
        producto.setIva_neto(new BigDecimal("42"));
        producto.setPrecioVentaPublico(new BigDecimal("200"));
        producto.setPrecioLista(new BigDecimal("242"));        
        assertTrue("El precio unitario no es el esperado",
                facturaService.calcularPrecioUnitario(Movimiento.VENTA, TipoDeComprobante.FACTURA_B, producto)
                        .compareTo(new BigDecimal("242")) == 0);
    }

    @Test
    public void shouldCalcularPrecioUnitarioWhenEsUnaVentaConFacturaC() {
        Producto producto = new Producto();
        producto.setPrecioCosto(new BigDecimal("100"));
        producto.setGanancia_neto(new BigDecimal("100"));
        producto.setIva_porcentaje(new BigDecimal("21"));
        producto.setIva_neto(new BigDecimal("42"));
        producto.setPrecioVentaPublico(new BigDecimal("200"));
        producto.setPrecioLista(new BigDecimal("242"));
        assertTrue("El precio unitario no es el esperado",
                facturaService.calcularPrecioUnitario(Movimiento.VENTA, TipoDeComprobante.FACTURA_C, producto)
                        .compareTo(new BigDecimal("242")) == 0);
    }

    @Test
    public void shouldCalcularPrecioUnitarioWhenVentaYFacturaY() {
        Producto producto = new Producto();
        producto.setPrecioCosto(new BigDecimal("100"));
        producto.setGanancia_neto(new BigDecimal("100"));
        producto.setIva_porcentaje(new BigDecimal("21"));
        producto.setIva_neto(new BigDecimal("42"));
        producto.setPrecioVentaPublico(new BigDecimal("200"));
        producto.setPrecioLista(new BigDecimal("242"));
        producto.setImpuestoInterno_porcentaje(BigDecimal.ZERO);
        assertTrue("El precio unitario no es el esperado",
                facturaService.calcularPrecioUnitario(Movimiento.VENTA, TipoDeComprobante.FACTURA_Y, producto)
                        .compareTo(new BigDecimal("221")) == 0);
    }
}
