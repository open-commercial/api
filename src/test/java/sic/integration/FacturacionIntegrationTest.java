package sic.integration;

import java.io.IOException;
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
   
    @Before
    public void setup() {
        String md5Test = "098f6bcd4621d373cade4e832627b4f6";
        usuarioRepository.save(new UsuarioBuilder().withNombre("test").withPassword(md5Test).build());
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
                .withCantidad(10)
                .withVentaMinima(1)
                .withPrecioVentaPublico(1000)
                .withIva_porcentaje(21.0)
                .withIva_neto(210)
                .withPrecioLista(1210)
                .build();
        ProductoDTO productoDos = new ProductoBuilder()
                .withCodigo("2")
                .withDescripcion("dos")
                .withCantidad(6)                               
                .withVentaMinima(1)
                .withPrecioVentaPublico(1000)
                .withIva_porcentaje(10.5)
                .withIva_neto(105)
                .withPrecioLista(1105)
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
        Assert.assertEquals(1210, renglonUno.getPrecioUnitario(), 0);
        Assert.assertEquals(242, renglonUno.getDescuento_neto(), 0);
        Assert.assertEquals(168, renglonUno.getIva_neto(), 0);
        Assert.assertEquals(4840, renglonUno.getImporte(), 0);
        Assert.assertEquals(1105, renglonDos.getPrecioUnitario(), 0);
        Assert.assertEquals(0, renglonDos.getDescuento_neto(), 0);
        Assert.assertEquals(105, renglonDos.getIva_neto(), 0);
        Assert.assertEquals(2210, renglonDos.getImporte(), 0);
        List<RenglonFactura> renglones = new ArrayList<>();
        renglones.add(renglonUno);
        renglones.add(renglonDos);     
        int size = renglones.size();
        double[] cantidades = new double[size];
        double[] ivaPorcentajeRenglones = new double[size];
        double[] ivaNetoRenglones = new double[size];
        int indice = 0;
        double subTotal = 0;
        for (RenglonFactura renglon : renglones) {
            subTotal += renglon.getImporte();
            cantidades[indice] = renglon.getCantidad();
            ivaPorcentajeRenglones[indice] = renglon.getIva_porcentaje();
            ivaNetoRenglones[indice] = renglon.getIva_neto();
            indice++;
        }
        assertEquals(7050, subTotal, 0);
        double descuentoPorcentaje = 25;
        double recargoPorcentaje = 10;
        double descuento_neto = (subTotal * descuentoPorcentaje) / 100;
        double recargo_neto = (subTotal * recargoPorcentaje) / 100;
        assertEquals(1762.5, descuento_neto, 0);
        assertEquals(705, recargo_neto, 0);
        indice = cantidades.length;
        double iva_105_netoFactura = 0;
        double iva_21_netoFactura = 0;
        for (int i = 0; i < indice; i++) {
            if (ivaPorcentajeRenglones[i] == 10.5) {
                iva_105_netoFactura += cantidades[i] * (ivaNetoRenglones[i]
                        - (ivaNetoRenglones[i] * (descuentoPorcentaje / 100))
                        + (ivaNetoRenglones[i] * (recargoPorcentaje / 100)));
            } else if (ivaPorcentajeRenglones[i] == 21) {
                iva_21_netoFactura += cantidades[i] * (ivaNetoRenglones[i]
                        - (ivaNetoRenglones[i] * (descuentoPorcentaje / 100))
                        + (ivaNetoRenglones[i] * (recargoPorcentaje / 100)));
            }
        }
        assertEquals(178.5, iva_105_netoFactura, 0);
        assertEquals(714, iva_21_netoFactura, 0);
        double subTotalBruto = subTotal + recargo_neto - descuento_neto - (iva_105_netoFactura + iva_21_netoFactura);
        assertEquals(5100, subTotalBruto, 0);
        double total = subTotalBruto + iva_105_netoFactura + iva_21_netoFactura;
        assertEquals(5992.5, total, 0);
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
                + "&idUsuario=" + (restTemplate.getForObject(apiPrefix + "/usuarios/busqueda?nombre=test", Usuario.class)).getId_Usuario()
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
        assertEquals(facturaVentaB.getSubTotal(), facturasRecuperadas.get(0).getSubTotal(), 0);
        assertEquals(facturaVentaB.getRecargo_neto(), facturasRecuperadas.get(0).getRecargo_neto(), 0);
        assertEquals(facturaVentaB.getSubTotal_bruto(), facturasRecuperadas.get(0).getSubTotal_bruto(), 0);
        assertEquals(facturaVentaB.getIva_105_neto(), facturasRecuperadas.get(0).getIva_105_neto(), 0);
        assertEquals(facturaVentaB.getIva_21_neto(), facturasRecuperadas.get(0).getIva_21_neto(), 0);
        assertEquals(facturaVentaB.getImpuestoInterno_neto(), facturasRecuperadas.get(0).getImpuestoInterno_neto(), 0);
        assertEquals(facturaVentaB.getTotal(), facturasRecuperadas.get(0).getTotal(), 0);
        RenglonFactura[] renglonesDeFacturaRecuperada = restTemplate.getForObject(apiPrefix + "/facturas/" + facturasRecuperadas.get(0).getId_Factura() + "/renglones", RenglonFactura[].class);
        if (renglonesDeFacturaRecuperada.length != 2) {
            Assert.fail("La factura no deberia tener mas de dos renglones");
        }
        assertEquals(renglonesDeFacturaRecuperada[0].getCantidad(), renglones.get(0).getCantidad(), 0);
        assertEquals(renglonesDeFacturaRecuperada[0].getCodigoItem(), renglones.get(0).getCodigoItem());
        assertEquals(renglonesDeFacturaRecuperada[0].getDescripcionItem(), renglones.get(0).getDescripcionItem());
        assertEquals(renglonesDeFacturaRecuperada[0].getDescuento_neto(), renglones.get(0).getDescuento_neto(), 0);
        assertEquals(renglonesDeFacturaRecuperada[0].getDescuento_porcentaje(), renglones.get(0).getDescuento_porcentaje(), 0);
        assertEquals(renglonesDeFacturaRecuperada[0].getGanancia_neto(), renglones.get(0).getGanancia_neto(), 0);
        assertEquals(renglonesDeFacturaRecuperada[0].getGanancia_porcentaje(), renglones.get(0).getGanancia_porcentaje(), 0);
        assertEquals(renglonesDeFacturaRecuperada[0].getImporte(), renglones.get(0).getImporte(), 0);
        assertEquals(renglonesDeFacturaRecuperada[0].getImpuesto_neto(), renglones.get(0).getImpuesto_neto(), 0);
        assertEquals(renglonesDeFacturaRecuperada[0].getImpuesto_porcentaje(), renglones.get(0).getImpuesto_porcentaje(), 0);
        assertEquals(renglonesDeFacturaRecuperada[0].getIva_neto(), renglones.get(0).getIva_neto(), 0);
        assertEquals(renglonesDeFacturaRecuperada[0].getIva_porcentaje(), renglones.get(0).getIva_porcentaje(), 0);
        assertEquals(renglonesDeFacturaRecuperada[0].getMedidaItem(), renglones.get(0).getMedidaItem());
        assertEquals(renglonesDeFacturaRecuperada[0].getPrecioUnitario(), renglones.get(0).getPrecioUnitario(), 0); 
        restTemplate.getForObject(apiPrefix + "/facturas/"+ facturasRecuperadas.get(0).getId_Factura() + "/reporte", byte[].class);
        uri = apiPrefix + "/productos/disponibilidad-stock?idProducto=" + productoUno.getId_Producto() + "," + productoDos.getId_Producto() + "&cantidad=5,4";
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
                .withCantidad(10)
                .withVentaMinima(1)
                .withPrecioVentaPublico(2000)                
                .withIva_porcentaje(21.0)
                .withIva_neto(420)
                .withPrecioLista(2420)
                .build();
        ProductoDTO productoDos = new ProductoBuilder()
                .withCodigo("2")
                .withDescripcion("dos")
                .withCantidad(6)              
                .withVentaMinima(1)
                .withPrecioVentaPublico(2000)                
                .withIva_porcentaje(10.5)
                .withIva_neto(210)
                .withPrecioLista(2210)
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
        double subTotal = 0;
        subTotal = renglonesPedido.stream()
                                  .map(renglon -> renglon.getSubTotal())
                                  .reduce(subTotal, (accumulator, item) -> accumulator + item);       
        PedidoDTO pedido = new PedidoDTO();    
        pedido.setRenglones(renglonesPedido);
        pedido.setTotalEstimado(subTotal);
        pedido.setObservaciones("Pedido Test");        
        PedidoDTO pedidoRecuperado = restTemplate.postForObject(apiPrefix + "/pedidos?idEmpresa=" + empresa.getId_Empresa()
                + "&idCliente=" + cliente.getId_Cliente()
                + "&idUsuario=" + (restTemplate.getForObject(apiPrefix + "/usuarios/busqueda?nombre=test", Usuario.class)).getId_Usuario(), pedido, PedidoDTO.class);
        assertEquals(pedido.getTotalEstimado(), pedidoRecuperado.getTotalEstimado(), 0);
        assertEquals(pedido.getObservaciones(), pedidoRecuperado.getObservaciones());
        assertEquals(pedidoRecuperado.getEstado(), EstadoPedido.ABIERTO);
        RenglonPedidoDTO[] renglonesDelPedido = restTemplate.getForObject(apiPrefix + "/pedidos/" + pedidoRecuperado.getId_Pedido() +"/renglones", RenglonPedidoDTO[].class);
        for (int i = 0; i < renglonesDelPedido.length; i++) {
            assertEquals(renglonesPedido.get(i).getCantidad(), renglonesDelPedido[i].getCantidad(), 0);
            assertEquals(renglonesPedido.get(i).getDescuento_neto(), renglonesDelPedido[i].getDescuento_neto(), 0);
            assertEquals(renglonesPedido.get(i).getDescuento_porcentaje(), renglonesDelPedido[i].getDescuento_porcentaje(), 0);
            assertEquals(renglonesPedido.get(i).getProducto(), renglonesDelPedido[i].getProducto());
            assertEquals(renglonesPedido.get(i).getSubTotal(), renglonesDelPedido[i].getSubTotal(), 0);
        }
        RenglonFactura[] renglonesParaFacturar = restTemplate.getForObject(apiPrefix + "/facturas/renglones/pedidos/" + pedidoRecuperado.getId_Pedido()
                + "?tipoDeComprobante=" + TipoDeComprobante.FACTURA_A, RenglonFactura[].class);                   
        subTotal = renglonesParaFacturar[0].getImporte();         
        assertEquals(8500, subTotal, 0);
        double recargoPorcentaje = 10;
        double recargo_neto = (subTotal * recargoPorcentaje) / 100;
        assertEquals(850, recargo_neto, 0);   
        double iva_105_netoFactura = 0;
        double iva_21_netoFactura = 0;
        if (renglonesParaFacturar[0].getIva_porcentaje() == 10.5) {
            iva_105_netoFactura += renglonesParaFacturar[0].getCantidad() * (renglonesParaFacturar[0].getIva_neto());
        } else if (renglonesParaFacturar[0].getIva_porcentaje() == 21) {
            iva_21_netoFactura += renglonesParaFacturar[0].getCantidad() * (renglonesParaFacturar[0].getIva_neto());
        }
        assertEquals(0, iva_105_netoFactura, 0);        
        assertEquals(1785, iva_21_netoFactura, 0);
        double subTotalBruto = subTotal + recargo_neto;
        assertEquals(9350, subTotalBruto, 0);
        double total = subTotalBruto + iva_105_netoFactura + iva_21_netoFactura;
        assertEquals(11135, total, 0);
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
                + "&idUsuario=" + (restTemplate.getForObject(apiPrefix + "/usuarios/busqueda?nombre=test", Usuario.class)).getId_Usuario()
                + "&idTransportista=" + transportista.getId_Transportista(), facturaVentaA, FacturaVenta[].class);
        List<FacturaVenta> facturasRecuperadas = restTemplate
                .exchange(apiPrefix + "/facturas/venta/busqueda/criteria?"
                        + "idEmpresa=" + empresa.getId_Empresa()
                        + "&nroPedido=" + pedidoRecuperado.getNroPedido(), HttpMethod.GET, null,
                        new ParameterizedTypeReference<PaginaRespuestaRest<FacturaVenta>>() {
                })
                .getBody().getContent();       
        assertEquals(1, facturasRecuperadas.size(), 0);  
        assertTrue("La factura se encuentra impaga", facturasRecuperadas.get(0).isPagada());
        pedidoRecuperado = restTemplate.getForObject(apiPrefix + "/pedidos/" + pedidoRecuperado.getId_Pedido(), PedidoDTO.class);
        assertEquals(EstadoPedido.ACTIVO, pedidoRecuperado.getEstado());
        renglonesDelPedido = restTemplate.getForObject(apiPrefix + "/pedidos/"+ pedidoRecuperado.getId_Pedido() +"/renglones", RenglonPedidoDTO[].class);
        assertEquals(renglones.get(0).getCantidad(), renglonesDelPedido[0].getCantidad(), 0);
        assertEquals(renglones.get(0).getDescuento_porcentaje(), renglonesDelPedido[0].getDescuento_porcentaje(), 0);
        // assert not ???
        //assertEquals(renglones.get(0).getDescuento_neto(), renglonesDelPedido[0].getDescuento_neto(), 0);                
        renglonesParaFacturar = restTemplate.getForObject(apiPrefix + "/facturas/renglones/pedidos/" + pedidoRecuperado.getId_Pedido()
                + "?tipoDeComprobante=" + TipoDeComprobante.FACTURA_B, RenglonFactura[].class); 
        subTotal = renglonesParaFacturar[0].getImporte();
        assertEquals(4420, subTotal, 0);        
        recargo_neto = (subTotal * recargoPorcentaje) / 100;
        assertEquals(442, recargo_neto, 0);
        iva_105_netoFactura = 0;
        iva_21_netoFactura = 0;
        if (renglonesParaFacturar[0].getIva_porcentaje() == 10.5) {
            iva_105_netoFactura += renglonesParaFacturar[0].getCantidad() * (renglonesParaFacturar[0].getIva_neto()
                    + (renglonesParaFacturar[0].getIva_neto() * (recargoPorcentaje / 100)));
        } else if (renglonesParaFacturar[0].getIva_porcentaje() == 21) {
            iva_21_netoFactura += renglonesParaFacturar[0].getCantidad() * (renglonesParaFacturar[0].getIva_neto()
                    + (renglonesParaFacturar[0].getIva_neto() * (recargoPorcentaje / 100)));
        }
        assertEquals(462, iva_105_netoFactura, 0);
        assertEquals(0, iva_21_netoFactura, 0);
        subTotalBruto = subTotal + recargo_neto - (iva_105_netoFactura + iva_21_netoFactura);
        assertEquals(4400, subTotalBruto, 0);                
        total = subTotalBruto + iva_105_netoFactura + iva_21_netoFactura;
        assertEquals(4862, total, 0);
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
                + "&idUsuario=" + (restTemplate.getForObject(apiPrefix + "/usuarios/busqueda?nombre=test", Usuario.class)).getId_Usuario()
                + "&idTransportista=" + transportista.getId_Transportista(), facturaVentaB, FacturaVenta[].class);
        facturasRecuperadas = restTemplate.exchange(apiPrefix + "/facturas/venta/busqueda/criteria?"
                + "idEmpresa=" + empresa.getId_Empresa()
                + "&nroPedido=" + pedidoRecuperado.getNroPedido(), HttpMethod.GET, null,
                new ParameterizedTypeReference<PaginaRespuestaRest<FacturaVenta>>() {
        }).getBody().getContent();            
        assertEquals(renglones.get(0).getCantidad(), renglonesDelPedido[1].getCantidad(), 0);
        assertEquals(renglones.get(0).getDescuento_porcentaje(), renglonesDelPedido[1].getDescuento_porcentaje(), 0);
        assertEquals(renglones.get(0).getDescuento_neto(), renglonesDelPedido[1].getDescuento_neto(), 0);    
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