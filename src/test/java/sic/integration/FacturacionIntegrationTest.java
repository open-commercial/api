package sic.integration;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClientResponseException;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.springframework.test.annotation.DirtiesContext;
import sic.builder.ClienteBuilder;
import sic.builder.CondicionIVABuilder;
import sic.builder.EmpresaBuilder;
import sic.builder.FormaDePagoBuilder;
import sic.builder.LocalidadBuilder;
import sic.builder.MedidaBuilder;
import sic.builder.ProductoBuilder;
import sic.builder.ProveedorBuilder;
import sic.builder.RubroBuilder;
import sic.builder.TransportistaBuilder;
import sic.builder.UsuarioBuilder;
import sic.modelo.Cliente;
import sic.modelo.CondicionIVA;
import sic.modelo.Credencial;
import sic.modelo.Empresa;
import sic.modelo.FacturaVenta;
import sic.modelo.FormaDePago;
import sic.modelo.Localidad;
import sic.modelo.Medida;
import sic.modelo.Movimiento;
import sic.modelo.Pais;
import sic.modelo.Proveedor;
import sic.modelo.Provincia;
import sic.modelo.RenglonFactura;
import sic.modelo.Rol;
import sic.modelo.Rubro;
import sic.modelo.TipoDeComprobante;
import sic.modelo.Transportista;
import sic.modelo.Usuario;
import sic.modelo.dto.FacturaVentaDTO;
import sic.repository.UsuarioRepository;
import sic.builder.RenglonPedidoBuilder;
import sic.modelo.EstadoPedido;
import sic.modelo.dto.PedidoDTO;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import sic.modelo.Producto;
import sic.modelo.dto.ProductoDTO;
import sic.modelo.dto.RenglonPedidoDTO;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class FacturacionIntegrationTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    private String token;
    
    private final String apiPrefix = "/api/v1";
    
    private final static BigDecimal IVA_21 = new BigDecimal("21");
    private final static BigDecimal IVA_105 = new BigDecimal("10.5");
    private final static BigDecimal CIEN = new BigDecimal("100");
   
    @Before
    public void setup() {
        String md5Test = "098f6bcd4621d373cade4e832627b4f6";
        usuarioRepository.save(new UsuarioBuilder().withUsername("test")
                                                   .withPassword(md5Test)
                                                   .withNombre("test")
                                                   .withApellido("test")
                                                   .withHabilitado(true)
                                                   .build());
        // Interceptor de RestTemplate para JWT
        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
        interceptors.add((ClientHttpRequestInterceptor) (HttpRequest request, byte[] body, ClientHttpRequestExecution execution) -> {
            request.getHeaders().set("Authorization", "Bearer " + token);
            return execution.execute(request, body);
        });        
        restTemplate.getRestTemplate().setInterceptors(interceptors);
        // ErrorHandler para RestTemplate        
        restTemplate.getRestTemplate().setErrorHandler(new ResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse response) throws IOException {
                HttpStatus.Series series = response.getStatusCode().series();
                return (HttpStatus.Series.CLIENT_ERROR.equals(series) || HttpStatus.Series.SERVER_ERROR.equals(series));
            }

            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                String mensaje = IOUtils.toString(response.getBody());                
                throw new RestClientResponseException(mensaje, response.getRawStatusCode(),
                        response.getStatusText(), response.getHeaders(),
                        null, Charset.defaultCharset());
            }
        });
    }
    
    @Test
    public void testFacturarConComprobanteB() {
        this.token = restTemplate.postForEntity(apiPrefix + "/login", new Credencial("test", "test"), String.class).getBody();
        Localidad localidad = new LocalidadBuilder().build();
        localidad.getProvincia().setPais(restTemplate.postForObject(apiPrefix + "/paises", localidad.getProvincia().getPais(), Pais.class));
        localidad.setProvincia(restTemplate.postForObject(apiPrefix + "/provincias", localidad.getProvincia(), Provincia.class));
        CondicionIVA condicionIVA = new CondicionIVABuilder().build();          
        Empresa empresa = new EmpresaBuilder()
                .withLocalidad(restTemplate.postForObject(apiPrefix + "/localidades", localidad, Localidad.class))
                .withCondicionIVA(restTemplate.postForObject(apiPrefix + "/condiciones-iva", condicionIVA, CondicionIVA.class))
                .build();
        empresa = restTemplate.postForObject(apiPrefix + "/empresas", empresa, Empresa.class);
        FormaDePago formaDePago = new FormaDePagoBuilder()
                .withAfectaCaja(false)
                .withEmpresa(empresa)
                .withPredeterminado(true)
                .withNombre("Efectivo")
                .build();
        restTemplate.postForObject(apiPrefix + "/formas-de-pago", formaDePago, FormaDePago.class);
        Usuario credencial = new UsuarioBuilder()
                .withId_Usuario(1)
                .withEliminado(false)
                .withNombre("Marcelo Cruz")
                .withPassword("marce")
                .withToken("yJhbGci1NiIsInR5cCI6IkpXVCJ9.eyJub21icmUiOiJjZWNpbGlvIn0.MCfaorSC7Wdc8rSW7BJizasfzsa")
                .withRol(new ArrayList<>())
                .build();
        Usuario viajante = new UsuarioBuilder()
                .withId_Usuario(1)
                .withEliminado(false)
                .withNombre("Fernando Aguirre")
                .withPassword("fernando")
                .withToken("yJhbGci1NiIsInR5cCI6IkpXVCJ9.eyJub21icmUiOiJjZWNpbGlvIn0.MCfaorSC7Wdc8rSW7BJizasfzsb")
                .withRol(new ArrayList<>(Arrays.asList(Rol.VIAJANTE)))
                .build();
        Cliente cliente = new ClienteBuilder()
                .withEmpresa(empresa)
                .withCondicionIVA(empresa.getCondicionIVA())
                .withLocalidad(empresa.getLocalidad())
                .withPredeterminado(true)
                .withCredencial(credencial)
                .withViajante(viajante)
                .build();
        cliente = restTemplate.postForObject(apiPrefix + "/clientes", cliente, Cliente.class);
        Transportista transportista = new TransportistaBuilder()
                .withEmpresa(empresa)
                .withLocalidad(empresa.getLocalidad())
                .build();
        transportista = restTemplate.postForObject(apiPrefix + "/transportistas", transportista, Transportista.class);
        Medida medida = new MedidaBuilder().withEmpresa(empresa).build();
        medida = restTemplate.postForObject(apiPrefix + "/medidas", medida, Medida.class);
        Proveedor proveedor = new ProveedorBuilder().withEmpresa(empresa)
                .withLocalidad(empresa.getLocalidad())
                .withCondicionIVA(empresa.getCondicionIVA())
                .build();
        proveedor = restTemplate.postForObject(apiPrefix + "/proveedores", proveedor, Proveedor.class);
        Rubro rubro = new RubroBuilder().withEmpresa(empresa).build();
        rubro = restTemplate.postForObject(apiPrefix + "/rubros", rubro, Rubro.class);
        ProductoDTO productoUno = new ProductoBuilder()
                .withCodigo("1")
                .withDescripcion("uno")
                .withCantidad(BigDecimal.TEN)
                .withVentaMinima(BigDecimal.ONE)
                .withPrecioCosto(CIEN)
                .withGanancia_porcentaje(new BigDecimal("900"))
                .withGanancia_neto(new BigDecimal("900"))
                .withPrecioVentaPublico(new BigDecimal("1000"))
                .withIva_porcentaje(new BigDecimal("21.0"))
                .withIva_neto(new BigDecimal("210"))
                .withPrecioLista(new BigDecimal("1210"))
                .build();
        ProductoDTO productoDos = new ProductoBuilder()
                .withCodigo("2")
                .withDescripcion("dos")
                .withCantidad(new BigDecimal("6"))                               
                .withVentaMinima(BigDecimal.ONE)
                .withPrecioCosto(CIEN)
                .withGanancia_porcentaje(new BigDecimal("900"))
                .withGanancia_neto(new BigDecimal("900"))
                .withPrecioVentaPublico(new BigDecimal("1000"))
                .withIva_porcentaje(new BigDecimal("10.5"))
                .withIva_neto(new BigDecimal("105"))
                .withPrecioLista(new BigDecimal("1105"))
                .build();        
        productoUno = restTemplate.postForObject(apiPrefix + "/productos?idMedida=" + medida.getId_Medida() + "&idRubro=" + rubro.getId_Rubro()
                + "&idProveedor=" + proveedor.getId_Proveedor() + "&idEmpresa=" + empresa.getId_Empresa(),
                productoUno, ProductoDTO.class);        
        productoDos = restTemplate.postForObject(apiPrefix + "/productos?idMedida=" + medida.getId_Medida() + "&idRubro=" + rubro.getId_Rubro()
                + "&idProveedor=" + proveedor.getId_Proveedor() + "&idEmpresa=" + empresa.getId_Empresa(),
                productoDos, ProductoDTO.class);
        String uri = apiPrefix + "/productos/disponibilidad-stock?idProducto=" + productoUno.getId_Producto() + "," + productoDos.getId_Producto() + "&cantidad=10,6";
        Assert.assertTrue(restTemplate.exchange(uri, HttpMethod.GET, null, new ParameterizedTypeReference<Map<Double, Producto>>() {}).getBody().isEmpty());
        RenglonFactura renglonUno = restTemplate.getForObject(apiPrefix + "/facturas/renglon?"
                + "idProducto=" + productoUno.getId_Producto()
                + "&tipoDeComprobante=" + TipoDeComprobante.FACTURA_B
                + "&movimiento=" + Movimiento.VENTA
                + "&cantidad=5" 
                + "&descuentoPorcentaje=20",
                RenglonFactura.class);
        RenglonFactura renglonDos = restTemplate.getForObject(apiPrefix + "/facturas/renglon?"
                + "idProducto=" + productoDos.getId_Producto()
                + "&tipoDeComprobante=" + TipoDeComprobante.FACTURA_B 
                + "&movimiento=" + Movimiento.VENTA
                + "&cantidad=2" 
                + "&descuentoPorcentaje=0",
                RenglonFactura.class);                
        assertTrue("El precio unitario no es el correcto", renglonUno.getPrecioUnitario().compareTo(new BigDecimal("1210")) == 0);
        assertTrue("El descuento neto no es correcto", renglonUno.getDescuento_neto().compareTo(new BigDecimal("242")) == 0);
        assertTrue("El iva neto no es el correcto", renglonUno.getIva_neto().compareTo(new BigDecimal("168")) == 0);
        assertTrue("El importe no es correcto", renglonUno.getImporte().compareTo(new BigDecimal("4840")) == 0);
        assertTrue("El precio unitario no es correcto", renglonDos.getPrecioUnitario().compareTo(new BigDecimal("1105")) == 0);
        assertTrue("El descuento neto no es correcto", renglonDos.getDescuento_neto().compareTo(BigDecimal.ZERO) == 0);
        assertTrue("El iva neto no es correcto", renglonDos.getIva_neto().compareTo(new BigDecimal("105")) == 0);
        assertTrue("El importe no es correcto", renglonDos.getImporte().compareTo(new BigDecimal("2210")) == 0);
        List<RenglonFactura> renglones = new ArrayList<>();
        renglones.add(renglonUno);
        renglones.add(renglonDos);     
        int size = renglones.size();
        BigDecimal[] cantidades = new BigDecimal[size];
        BigDecimal[] ivaPorcentajeRenglones = new BigDecimal[size];
        BigDecimal[] ivaNetoRenglones = new BigDecimal[size];
        int indice = 0;
        BigDecimal subTotal = BigDecimal.ZERO;
        for (RenglonFactura renglon : renglones) {
            subTotal = subTotal.add(renglon.getImporte());
            cantidades[indice] = renglon.getCantidad();
            ivaPorcentajeRenglones[indice] = renglon.getIva_porcentaje();
            ivaNetoRenglones[indice] = renglon.getIva_neto();
            indice++;
        }
        assertTrue("El subtotal no es correcto", subTotal.compareTo(new BigDecimal("7050")) == 0);
        BigDecimal descuentoPorcentaje = new BigDecimal("25");
        BigDecimal recargoPorcentaje = new BigDecimal("10");
        BigDecimal descuento_neto = subTotal.multiply(descuentoPorcentaje).divide(CIEN, 15, RoundingMode.HALF_UP);
        BigDecimal recargo_neto = subTotal.multiply(recargoPorcentaje).divide(CIEN, 15, RoundingMode.HALF_UP);
        assertTrue("El descuento neto no es el esperado", descuento_neto.compareTo(new BigDecimal("1762.5")) == 0);
        assertTrue("El recargo neto no es el esperado", recargo_neto.compareTo(new BigDecimal("705")) == 0);
        indice = cantidades.length;
        BigDecimal iva_105_netoFactura = BigDecimal.ZERO;
        BigDecimal iva_21_netoFactura = BigDecimal.ZERO;
        for (int i = 0; i < indice; i++) {
            if (ivaPorcentajeRenglones[i].compareTo(IVA_105) == 0) {
                iva_105_netoFactura = iva_105_netoFactura.add(cantidades[i].multiply(ivaNetoRenglones[i].subtract(ivaNetoRenglones[i].multiply(descuentoPorcentaje.divide(CIEN, 15, RoundingMode.HALF_UP)))
                        .add(ivaNetoRenglones[i].multiply(recargoPorcentaje.divide(CIEN, 15, RoundingMode.HALF_UP)))));
            } else if (ivaPorcentajeRenglones[i].compareTo(IVA_21) == 0) {
                iva_21_netoFactura = iva_21_netoFactura.add(cantidades[i].multiply(ivaNetoRenglones[i].subtract(ivaNetoRenglones[i].multiply(descuentoPorcentaje.divide(CIEN, 15, RoundingMode.HALF_UP)))
                        .add(ivaNetoRenglones[i].multiply(recargoPorcentaje.divide(CIEN, 15, RoundingMode.HALF_UP)))));
            }
        }
        assertTrue("El iva neto 10.5 no es el esperado", iva_105_netoFactura.compareTo(new BigDecimal("178.5")) == 0);
        assertTrue("El iva neto 21 no es el esperado", iva_21_netoFactura.compareTo(new BigDecimal("714")) == 0);
        BigDecimal subTotalBruto = subTotal.add(recargo_neto).subtract(descuento_neto).subtract(iva_105_netoFactura.add(iva_21_netoFactura));
        assertTrue("El subtotal bruto no es el esperado", subTotalBruto.compareTo(new BigDecimal("5100")) == 0);
        BigDecimal total = subTotalBruto.add(iva_105_netoFactura).add(iva_21_netoFactura);
        assertTrue("El total no es el esperado", total.doubleValue() == 5992.5);
        FacturaVentaDTO facturaVentaB = new FacturaVentaDTO();
        facturaVentaB.setTipoComprobante(TipoDeComprobante.FACTURA_B);
        facturaVentaB.setRenglones(renglones);
        facturaVentaB.setSubTotal(subTotal);
        facturaVentaB.setRecargo_porcentaje(recargoPorcentaje);
        facturaVentaB.setRecargo_neto(recargo_neto);
        facturaVentaB.setDescuento_porcentaje(descuentoPorcentaje);
        facturaVentaB.setDescuento_neto(descuento_neto);
        facturaVentaB.setSubTotal_bruto(subTotalBruto);
        facturaVentaB.setIva_105_neto(iva_105_netoFactura);
        facturaVentaB.setIva_21_neto(iva_21_netoFactura);        
        facturaVentaB.setTotal(total);        
        restTemplate.postForObject(apiPrefix + "/facturas/venta?"
                + "idCliente=" + cliente.getId_Cliente()
                + "&idEmpresa=" + empresa.getId_Empresa()
                + "&idUsuario=" + credencial.getId_Usuario()
                + "&idTransportista=" + transportista.getId_Transportista(), facturaVentaB, FacturaVenta[].class);
        List<FacturaVenta> facturasRecuperadas = restTemplate
                .exchange(apiPrefix + "/facturas/venta/busqueda/criteria?idEmpresa=1&tipoFactura=B&nroSerie=0&nroFactura=1", HttpMethod.GET, null,
                        new ParameterizedTypeReference<PaginaRespuestaRest<FacturaVenta>>() {
                })
                .getBody().getContent();
        if (facturasRecuperadas.size() != 1) {
            Assert.fail("Debería existir exactamente una factura");
        }
        assertEquals(facturaVentaB.getNombreEmpresa(), "Globo Corporation");
        assertEquals(facturaVentaB.getTipoComprobante(), facturasRecuperadas.get(0).getTipoComprobante());      
        assertTrue("El sub total no es el esperado", facturasRecuperadas.get(0).getSubTotal().compareTo(facturaVentaB.getSubTotal()) == 0);
        assertTrue("El recargo neto no es el esperado", facturasRecuperadas.get(0).getRecargo_neto().compareTo(facturaVentaB.getRecargo_neto()) == 0);
        assertTrue("El total no es el esperado", facturasRecuperadas.get(0).getSubTotal_bruto().compareTo(facturaVentaB.getSubTotal_bruto()) == 0);
        assertTrue("El iva 10.5 no es el esperado", facturasRecuperadas.get(0).getIva_105_neto().compareTo(facturaVentaB.getIva_105_neto()) == 0);
        assertTrue("El iva 21 no es el esperado", facturasRecuperadas.get(0).getIva_21_neto().compareTo(facturaVentaB.getIva_21_neto()) == 0);
        assertTrue("El impuesto interno no es el esperado", facturasRecuperadas.get(0).getImpuestoInterno_neto().compareTo(facturaVentaB.getImpuestoInterno_neto()) == 0);
        assertTrue("El total no es el esperado", facturasRecuperadas.get(0).getTotal().compareTo(facturaVentaB.getTotal()) == 0);
        RenglonFactura[] renglonesDeFacturaRecuperada = restTemplate.getForObject(apiPrefix + "/facturas/" + facturasRecuperadas.get(0).getId_Factura() + "/renglones", RenglonFactura[].class);
        if (renglonesDeFacturaRecuperada.length != 2) {
            Assert.fail("La factura no deberia tener mas de dos renglones");
        }
        assertTrue("La cantidad no es la esperada", renglonesDeFacturaRecuperada[0].getCantidad().compareTo(renglones.get(0).getCantidad()) == 0);
        assertTrue("El descuento neto no es el esperado", renglonesDeFacturaRecuperada[0].getDescuento_neto().compareTo(renglones.get(0).getDescuento_neto()) == 0);
        assertTrue("El descuento porcentaje no es el esperado", renglonesDeFacturaRecuperada[0].getDescuento_porcentaje().compareTo(renglones.get(0).getDescuento_porcentaje()) == 0);
        assertTrue("La ganancia neta no es la esperada", renglonesDeFacturaRecuperada[0].getGanancia_neto().compareTo(renglones.get(0).getGanancia_neto()) == 0);
        assertTrue("La ganancia porcentaje no es la esperada", renglonesDeFacturaRecuperada[0].getGanancia_porcentaje().compareTo(renglones.get(0).getGanancia_porcentaje()) == 0);
        assertTrue("El importe no es el esperado", renglonesDeFacturaRecuperada[0].getImporte().compareTo(renglones.get(0).getImporte()) == 0);
        assertTrue("El impuesto neto no es el esperado", renglonesDeFacturaRecuperada[0].getImpuesto_neto().compareTo(renglones.get(0).getImpuesto_neto()) == 0);
        assertTrue("El impuesto porcentaje no es el esperado", renglonesDeFacturaRecuperada[0].getImpuesto_porcentaje().compareTo(renglones.get(0).getImpuesto_porcentaje()) == 0);
        assertTrue("El iva neto no es el esperado", renglonesDeFacturaRecuperada[0].getIva_neto().compareTo(renglones.get(0).getIva_neto()) == 0);
        assertTrue("El iva porcentaje no es el esperado", renglonesDeFacturaRecuperada[0].getIva_porcentaje().compareTo(renglones.get(0).getIva_porcentaje()) == 0);
        assertTrue("El precio unitario no es el esperado", renglonesDeFacturaRecuperada[0].getPrecioUnitario().compareTo(renglones.get(0).getPrecioUnitario()) == 0);
        assertEquals(renglonesDeFacturaRecuperada[0].getCodigoItem(), renglones.get(0).getCodigoItem());
        assertEquals(renglonesDeFacturaRecuperada[0].getDescripcionItem(), renglones.get(0).getDescripcionItem());
        assertEquals(renglonesDeFacturaRecuperada[0].getMedidaItem(), renglones.get(0).getMedidaItem());
        restTemplate.getForObject(apiPrefix + "/facturas/"+ facturasRecuperadas.get(0).getId_Factura() + "/reporte", byte[].class);
        uri = apiPrefix + "/productos/disponibilidad-stock?idProducto=" + productoUno.getId_Producto() + "," + productoDos.getId_Producto() + "&cantidad=5,4";
        Assert.assertTrue(restTemplate.exchange(uri, HttpMethod.GET, null, new ParameterizedTypeReference<Map<Double, Producto>>() {}).getBody().isEmpty());
        restTemplate.delete(apiPrefix + "/facturas?idFactura=1");
        uri = apiPrefix + "/productos/disponibilidad-stock?idProducto=" + productoUno.getId_Producto() + "," + productoDos.getId_Producto() + "&cantidad=10,6";
        Assert.assertTrue(restTemplate.exchange(uri, HttpMethod.GET, null, new ParameterizedTypeReference<Map<Double, Producto>>() {}).getBody().isEmpty());
    }
    
    @Test
    public void testFacturarPedido() {
        token = restTemplate.postForEntity(apiPrefix + "/login", new Credencial("test", "test"), String.class).getBody();
        Localidad localidad = new LocalidadBuilder().build();
        localidad.getProvincia().setPais(restTemplate.postForObject(apiPrefix + "/paises", localidad.getProvincia().getPais(), Pais.class));
        localidad.setProvincia(restTemplate.postForObject(apiPrefix + "/provincias", localidad.getProvincia(), Provincia.class));
        CondicionIVA condicionIVA = new CondicionIVABuilder().build();          
        Empresa empresa = new EmpresaBuilder()
                .withLocalidad(restTemplate.postForObject(apiPrefix + "/localidades", localidad, Localidad.class))
                .withCondicionIVA(restTemplate.postForObject(apiPrefix + "/condiciones-iva", condicionIVA, CondicionIVA.class))
                .build();
        empresa = restTemplate.postForObject(apiPrefix + "/empresas", empresa, Empresa.class);
        FormaDePago formaDePago = new FormaDePagoBuilder()
                .withAfectaCaja(false)
                .withEmpresa(empresa)
                .withPredeterminado(true)
                .withNombre("Efectivo")
                .build();
        formaDePago = restTemplate.postForObject(apiPrefix + "/formas-de-pago", formaDePago, FormaDePago.class);
        Usuario credencial = new UsuarioBuilder()
                .withId_Usuario(1)
                .withEliminado(false)
                .withNombre("Marcelo Cruz")
                .withPassword("marce")
                .withToken("yJhbGci1NiIsInR5cCI6IkpXVCJ9.eyJub21icmUiOiJjZWNpbGlvIn0.MCfaorSC7Wdc8rSW7BJizasfzsa")
                .withRol(new ArrayList<>())
                .build();
        Cliente cliente = new ClienteBuilder()
                .withEmpresa(empresa)
                .withCondicionIVA(empresa.getCondicionIVA())
                .withLocalidad(empresa.getLocalidad())
                .withPredeterminado(true)
                .withCredencial(credencial)
                .withViajante(null)
                .build();
        cliente = restTemplate.postForObject(apiPrefix + "/clientes", cliente, Cliente.class);
        Transportista transportista = new TransportistaBuilder()
                .withEmpresa(empresa)
                .withLocalidad(empresa.getLocalidad())
                .build();
        transportista = restTemplate.postForObject(apiPrefix + "/transportistas", transportista, Transportista.class);
        Medida medida = new MedidaBuilder().withEmpresa(empresa).build();
        medida = restTemplate.postForObject(apiPrefix + "/medidas", medida, Medida.class);
        Proveedor proveedor = new ProveedorBuilder().withEmpresa(empresa)
                .withLocalidad(empresa.getLocalidad())
                .withCondicionIVA(empresa.getCondicionIVA())
                .build();
        proveedor = restTemplate.postForObject(apiPrefix + "/proveedores", proveedor, Proveedor.class);
        Rubro rubro = new RubroBuilder().withEmpresa(empresa).build();
        rubro = restTemplate.postForObject(apiPrefix + "/rubros", rubro, Rubro.class);
        ProductoDTO productoUno = new ProductoBuilder()
                .withCodigo("1")
                .withDescripcion("uno")
                .withCantidad(BigDecimal.TEN)
                .withVentaMinima(BigDecimal.ONE)
                .withPrecioCosto(new BigDecimal("200"))
                .withGanancia_porcentaje(new BigDecimal("900"))
                .withGanancia_neto(new BigDecimal("1800"))
                .withPrecioVentaPublico(new BigDecimal("2000"))                
                .withIva_porcentaje(new BigDecimal("21.0"))
                .withIva_neto(new BigDecimal("420"))
                .withPrecioLista(new BigDecimal("2420"))
                .build();
        ProductoDTO productoDos = new ProductoBuilder()
                .withCodigo("2")
                .withDescripcion("dos")
                .withCantidad(new BigDecimal("6"))              
                .withVentaMinima(BigDecimal.ONE)
                .withPrecioCosto(new BigDecimal("200"))
                .withGanancia_porcentaje(new BigDecimal("900"))
                .withGanancia_neto(new BigDecimal("1800"))
                .withPrecioVentaPublico(new BigDecimal("2000"))                
                .withIva_porcentaje(new BigDecimal("10.5"))
                .withIva_neto(new BigDecimal("210"))
                .withPrecioLista(new BigDecimal("2210"))
                .build();
        productoUno = restTemplate.postForObject(apiPrefix + "/productos?idMedida=" + medida.getId_Medida() + "&idRubro=" + rubro.getId_Rubro()
                + "&idProveedor=" + proveedor.getId_Proveedor() + "&idEmpresa=" + empresa.getId_Empresa(),
                productoUno, ProductoDTO.class);        
        productoDos = restTemplate.postForObject(apiPrefix + "/productos?idMedida=" + medida.getId_Medida() + "&idRubro=" + rubro.getId_Rubro()
                + "&idProveedor=" + proveedor.getId_Proveedor() + "&idEmpresa=" + empresa.getId_Empresa(),
                productoDos, ProductoDTO.class);
        String uri = apiPrefix + "/productos/disponibilidad-stock?idProducto=" + productoUno.getId_Producto() + "," + productoDos.getId_Producto() + "&cantidad=10,6";
        Assert.assertTrue(restTemplate.exchange(uri, HttpMethod.GET, null, new ParameterizedTypeReference<Map<Double, Producto>>() {}).getBody().isEmpty());
        RenglonFactura renglonUno = restTemplate.getForObject(apiPrefix + "/facturas/renglon?"
                + "idProducto=" + productoUno.getId_Producto()
                + "&tipoDeComprobante=" + TipoDeComprobante.FACTURA_B
                + "&movimiento=" + Movimiento.VENTA
                + "&cantidad=5" 
                + "&descuentoPorcentaje=15",
                RenglonFactura.class);        
        RenglonPedidoDTO renglonPedidoUno = new RenglonPedidoBuilder()
                .withCantidad(renglonUno.getCantidad())
                .withDescuentoPorcentaje(renglonUno.getDescuento_porcentaje())
                .withDescuentoNeto(renglonUno.getDescuento_neto())
                .withProducto(productoUno)
                .withSubTotal(renglonUno.getImporte())
                .build();                
        RenglonFactura renglonDos = restTemplate.getForObject(apiPrefix + "/facturas/renglon?"
                + "idProducto=" + productoDos.getId_Producto()
                + "&tipoDeComprobante=" + TipoDeComprobante.FACTURA_B 
                + "&movimiento=" + Movimiento.VENTA
                + "&cantidad=2"
                + "&descuentoPorcentaje=0",
                RenglonFactura.class);
        RenglonPedidoDTO renglonPedidoDos = new RenglonPedidoBuilder()
                .withCantidad(renglonDos.getCantidad())
                .withDescuentoPorcentaje(renglonDos.getDescuento_porcentaje())
                .withDescuentoNeto(renglonDos.getDescuento_neto())
                .withProducto(productoDos)
                .withSubTotal(renglonDos.getImporte())
                .build();             
        List<RenglonPedidoDTO> renglonesPedido = new ArrayList<>();
        renglonesPedido.add(renglonPedidoUno);
        renglonesPedido.add(renglonPedidoDos);
        BigDecimal subTotal = BigDecimal.ZERO;
        for (RenglonPedidoDTO renglon : renglonesPedido) {
            subTotal = subTotal.add(renglon.getSubTotal());
        }    
        PedidoDTO pedido = new PedidoDTO();    
        pedido.setRenglones(renglonesPedido);
        pedido.setTotalEstimado(subTotal);
        pedido.setObservaciones("Pedido Test");
        PedidoDTO pedidoRecuperado = restTemplate.postForObject(apiPrefix + "/pedidos?idEmpresa=" + empresa.getId_Empresa()
                + "&idCliente=" + cliente.getId_Cliente()
                + "&idUsuario=" + credencial.getId_Usuario(), pedido, PedidoDTO.class);
        assertTrue("El total estimado no es el esperado", pedidoRecuperado.getTotalEstimado().compareTo(pedido.getTotalEstimado()) == 0);
        assertEquals(pedido.getObservaciones(), pedidoRecuperado.getObservaciones());
        assertEquals(pedidoRecuperado.getEstado(), EstadoPedido.ABIERTO);
        RenglonPedidoDTO[] renglonesDelPedido = restTemplate.getForObject(apiPrefix + "/pedidos/" + pedidoRecuperado.getId_Pedido() + "/renglones", RenglonPedidoDTO[].class);
        for (int i = 0; i < renglonesDelPedido.length; i++) {
            assertTrue("La cantidad no es la esperada", renglonesPedido.get(i).getCantidad().compareTo(renglonesDelPedido[i].getCantidad()) == 0);
            assertTrue("El descuento neto no es el esperado", renglonesPedido.get(i).getDescuento_neto().compareTo(renglonesDelPedido[i].getDescuento_neto()) == 0);
            assertTrue("El descuento porcentaje no es el esperado", renglonesPedido.get(i).getDescuento_porcentaje().compareTo(renglonesDelPedido[i].getDescuento_porcentaje()) == 0);
            assertTrue("La sub total no es el esperado", renglonesPedido.get(i).getSubTotal().compareTo(renglonesDelPedido[i].getSubTotal()) == 0);
        }
        RenglonFactura[] renglonesParaFacturar = restTemplate.getForObject(apiPrefix + "/facturas/renglones/pedidos/" + pedidoRecuperado.getId_Pedido()
                + "?tipoDeComprobante=" + TipoDeComprobante.FACTURA_A, RenglonFactura[].class); 
        subTotal = renglonesParaFacturar[0].getImporte();  
        assertTrue("La importe no es el esperado", renglonesParaFacturar[0].getImporte().compareTo(new BigDecimal("8500")) == 0);
        BigDecimal recargoPorcentaje = BigDecimal.TEN;
        BigDecimal recargo_neto = subTotal.multiply(recargoPorcentaje).divide(CIEN, 15, RoundingMode.HALF_UP);
        assertTrue("El recargo neto no es el esperado" + recargo_neto.doubleValue(), recargo_neto.compareTo(new BigDecimal("850")) == 0);
        BigDecimal iva_105_netoFactura = BigDecimal.ZERO;
        BigDecimal iva_21_netoFactura = BigDecimal.ZERO;
        if (renglonesParaFacturar[0].getIva_porcentaje().compareTo(IVA_105) == 0) {
            iva_105_netoFactura = iva_105_netoFactura.add(renglonesParaFacturar[0].getCantidad().multiply(renglonesParaFacturar[0].getIva_neto()));
        } else if (renglonesParaFacturar[0].getIva_porcentaje().compareTo(IVA_21) == 0) {
            iva_21_netoFactura = iva_21_netoFactura.add(renglonesParaFacturar[0].getCantidad().multiply(renglonesParaFacturar[0].getIva_neto()));
        }
        assertTrue("El iva 10.5 neto no es el esperado", iva_105_netoFactura.compareTo(BigDecimal.ZERO) == 0);
        assertTrue("El iva 21 neto no es el esperado", iva_21_netoFactura.compareTo(new BigDecimal("1785")) == 0);
        BigDecimal subTotalBruto = subTotal.add(recargo_neto);
        assertTrue("El sub total bruto no es el esperado", subTotalBruto.compareTo(new BigDecimal("9350")) == 0);
        BigDecimal total = subTotalBruto.add(iva_105_netoFactura).add(iva_21_netoFactura);
        assertTrue("El total no es el esperado", total.compareTo(new BigDecimal("11135")) == 0);
        FacturaVentaDTO facturaVentaA = new FacturaVentaDTO();
        facturaVentaA.setTipoComprobante(TipoDeComprobante.FACTURA_A);
        List<RenglonFactura> renglones = new ArrayList<>();
        renglones.add(renglonesParaFacturar[0]);
        facturaVentaA.setRenglones(renglones);
        facturaVentaA.setSubTotal(subTotal);
        facturaVentaA.setRecargo_neto(recargo_neto);
        facturaVentaA.setSubTotal_bruto(subTotalBruto);
        facturaVentaA.setIva_105_neto(iva_105_netoFactura);
        facturaVentaA.setIva_21_neto(iva_21_netoFactura);        
        facturaVentaA.setTotal(total);
        facturaVentaA.setFecha(new Date());
        restTemplate.postForObject(apiPrefix + "/facturas/venta?idPedido=" + pedidoRecuperado.getId_Pedido()
                + "&idsFormaDePago=" + formaDePago.getId_FormaDePago()
                + "&montos=" + total
                + "&idCliente=" + cliente.getId_Cliente()
                + "&idEmpresa=" + empresa.getId_Empresa()
                + "&idUsuario=" + credencial.getId_Usuario()
                + "&idTransportista=" + transportista.getId_Transportista(), facturaVentaA, FacturaVenta[].class);
        List<FacturaVenta> facturasRecuperadas = restTemplate
                .exchange(apiPrefix + "/facturas/venta/busqueda/criteria?"
                        + "idEmpresa=" + empresa.getId_Empresa()
                        + "&nroPedido=" + pedidoRecuperado.getNroPedido(), HttpMethod.GET, null,
                        new ParameterizedTypeReference<PaginaRespuestaRest<FacturaVenta>>() {
                })
                .getBody().getContent();       
        assertEquals(1, facturasRecuperadas.size(), 0);  
        pedidoRecuperado = restTemplate.getForObject(apiPrefix + "/pedidos/" + pedidoRecuperado.getId_Pedido(), PedidoDTO.class);
        assertEquals(EstadoPedido.ACTIVO, pedidoRecuperado.getEstado());
        renglonesDelPedido = restTemplate.getForObject(apiPrefix + "/pedidos/"+ pedidoRecuperado.getId_Pedido() +"/renglones", RenglonPedidoDTO[].class);
        assertTrue("La cantidad no es la esperada", renglones.get(0).getCantidad().compareTo(renglonesDelPedido[0].getCantidad()) == 0);
        assertTrue("El descuento porcentaje no es el esperado", renglones.get(0).getDescuento_porcentaje().compareTo(renglonesDelPedido[0].getDescuento_porcentaje()) == 0);                
        renglonesParaFacturar = restTemplate.getForObject(apiPrefix + "/facturas/renglones/pedidos/" + pedidoRecuperado.getId_Pedido()
                + "?tipoDeComprobante=" + TipoDeComprobante.FACTURA_B, RenglonFactura[].class); 
        subTotal = renglonesParaFacturar[0].getImporte();
        assertTrue("La cantidad no es la esperado", subTotal.compareTo(new BigDecimal("4420")) == 0);
        recargo_neto =  subTotal.multiply(recargoPorcentaje).divide(CIEN, 15, RoundingMode.HALF_UP);
        assertTrue("El recargo neto no es la esperado", recargo_neto.compareTo(new BigDecimal("442")) == 0);
        iva_105_netoFactura = BigDecimal.ZERO;
        iva_21_netoFactura = BigDecimal.ZERO;
        if (renglonesParaFacturar[0].getIva_porcentaje().compareTo(IVA_105) == 0) {
            iva_105_netoFactura = iva_105_netoFactura.add(renglonesParaFacturar[0].getCantidad().multiply((renglonesParaFacturar[0].getIva_neto()
                    .add(renglonesParaFacturar[0].getIva_neto().multiply(recargoPorcentaje.divide(CIEN, 15, RoundingMode.HALF_UP))))));
        } else if (renglonesParaFacturar[0].getIva_porcentaje().compareTo(IVA_21) == 0) {
            iva_21_netoFactura = iva_21_netoFactura.add(renglonesParaFacturar[0].getCantidad().multiply((renglonesParaFacturar[0].getIva_neto()
                    .add(renglonesParaFacturar[0].getIva_neto().multiply(recargoPorcentaje.divide(CIEN, 15, RoundingMode.HALF_UP))))));
        }
        assertTrue("El iva 10.5 neto no es la esperado", iva_105_netoFactura.compareTo(new BigDecimal("462")) == 0);
        assertTrue("El iva 21 neto no es la esperado", iva_21_netoFactura.compareTo(BigDecimal.ZERO) == 0);
        subTotalBruto = subTotal.add(recargo_neto).subtract(iva_105_netoFactura.add(iva_21_netoFactura));
        assertTrue("El sub total bruto no es la esperado", subTotalBruto.compareTo(new BigDecimal("4400")) == 0);
        total = subTotalBruto.add(iva_105_netoFactura).add(iva_21_netoFactura);
        assertTrue("El recargo neto no es la esperado", total.compareTo(new BigDecimal("4862")) == 0);           
        FacturaVentaDTO facturaVentaB = new FacturaVentaDTO();
        facturaVentaB.setTipoComprobante(TipoDeComprobante.FACTURA_B);
        renglones.clear();
        renglones.add(renglonesParaFacturar[0]);
        facturaVentaB.setRenglones(renglones);
        facturaVentaB.setSubTotal(subTotal);
        facturaVentaB.setRecargo_porcentaje(recargoPorcentaje);
        facturaVentaB.setRecargo_neto(recargo_neto);
        facturaVentaB.setSubTotal_bruto(subTotalBruto);
        facturaVentaB.setIva_105_neto(iva_105_netoFactura);
        facturaVentaB.setIva_21_neto(iva_21_netoFactura);        
        facturaVentaB.setTotal(total);
        facturaVentaB.setFecha(new Date());
        restTemplate.postForObject(apiPrefix + "/facturas/venta?idPedido=" + pedidoRecuperado.getId_Pedido()
                + "&idCliente=" + cliente.getId_Cliente()
                + "&idEmpresa=" + empresa.getId_Empresa()
                + "&idUsuario=" + credencial.getId_Usuario()
                + "&idTransportista=" + transportista.getId_Transportista(), facturaVentaB, FacturaVenta[].class);
        facturasRecuperadas = restTemplate.exchange(apiPrefix + "/facturas/venta/busqueda/criteria?"
                + "idEmpresa=" + empresa.getId_Empresa()
                + "&nroPedido=" + pedidoRecuperado.getNroPedido(), HttpMethod.GET, null,
                new ParameterizedTypeReference<PaginaRespuestaRest<FacturaVenta>>() {
        }).getBody().getContent();            
        assertTrue("La cantidad no es la esperada", renglones.get(0).getCantidad().compareTo(renglonesDelPedido[1].getCantidad()) == 0); 
        assertTrue("El porcentaje de descuento no es la esperado", renglones.get(0).getDescuento_porcentaje().compareTo(renglonesDelPedido[1].getDescuento_porcentaje()) == 0);  
        assertTrue("El descuento no es el esperado", renglones.get(0).getDescuento_neto().compareTo(renglonesDelPedido[1].getDescuento_neto()) == 0); 
        assertEquals(2, facturasRecuperadas.size(), 0);        
        pedidoRecuperado = restTemplate.getForObject(apiPrefix + "/pedidos/" + pedidoRecuperado.getId_Pedido(), PedidoDTO.class);
        assertEquals(EstadoPedido.CERRADO, pedidoRecuperado.getEstado());
        restTemplate.delete(apiPrefix + "/facturas?idFactura=" + facturasRecuperadas.get(0).getId_Factura());
        pedidoRecuperado = restTemplate.getForObject(apiPrefix + "/pedidos/" + pedidoRecuperado.getId_Pedido(), PedidoDTO.class);
        assertEquals(EstadoPedido.ACTIVO, pedidoRecuperado.getEstado());
        restTemplate.delete(apiPrefix + "/facturas?idFactura=" + facturasRecuperadas.get(1).getId_Factura());
        pedidoRecuperado = restTemplate.getForObject(apiPrefix + "/pedidos/" + pedidoRecuperado.getId_Pedido(), PedidoDTO.class);
        assertEquals(EstadoPedido.ABIERTO, pedidoRecuperado.getEstado());
    }

}