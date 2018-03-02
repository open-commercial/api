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
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClientResponseException;
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
import sic.modelo.NotaCredito;
import sic.modelo.NotaDebito;
import sic.modelo.Pais;
import sic.modelo.Producto;
import sic.modelo.Proveedor;
import sic.modelo.Provincia;
import sic.modelo.Recibo;
import sic.modelo.RenglonCuentaCorriente;
import sic.modelo.RenglonFactura;
import sic.modelo.RenglonNotaCredito;
import sic.modelo.RenglonNotaDebito;
import sic.modelo.Rol;
import sic.modelo.Rubro;
import sic.modelo.TipoDeComprobante;
import sic.modelo.Transportista;
import sic.modelo.Usuario;
import sic.modelo.dto.FacturaCompraDTO;
import sic.modelo.dto.FacturaVentaDTO;
import sic.modelo.dto.NotaCreditoDTO;
import sic.modelo.dto.NotaDebitoDTO;
import sic.modelo.dto.ProductoDTO;
import sic.modelo.dto.ReciboDTO;
import sic.repository.UsuarioRepository;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class CuentaCorrienteIntegrationTest {

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
    public void testCuentaCorrienteCliente() {
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
        formaDePago = restTemplate.postForObject(apiPrefix + "/formas-de-pago", formaDePago, FormaDePago.class);
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
        Assert.assertTrue(restTemplate.exchange(uri, HttpMethod.GET, null, new ParameterizedTypeReference<Map<BigDecimal, Producto>>() {}).getBody().isEmpty());
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
        BigDecimal descuentoPorcentaje = new BigDecimal("25");
        BigDecimal recargoPorcentaje = new BigDecimal("10");
        BigDecimal descuento_neto = subTotal.multiply(descuentoPorcentaje).divide(CIEN, 15, RoundingMode.HALF_UP);
        BigDecimal recargo_neto = subTotal.multiply(recargoPorcentaje).divide(CIEN, 15, RoundingMode.HALF_UP);
        indice = cantidades.length;
        BigDecimal iva_105_netoFactura = BigDecimal.ZERO;
        BigDecimal iva_21_netoFactura = BigDecimal.ZERO;
        for (int i = 0; i < indice; i++) {
            if (ivaPorcentajeRenglones[i].compareTo(IVA_105) == 0) {
                iva_105_netoFactura = iva_105_netoFactura.add(cantidades[i].multiply(ivaNetoRenglones[i]
                .subtract(ivaNetoRenglones[i].multiply(descuentoPorcentaje.divide(CIEN, 15, RoundingMode.HALF_UP)))
                .add(ivaNetoRenglones[i].multiply(recargoPorcentaje.divide(CIEN, 15, RoundingMode.HALF_UP)))));
            } else if (ivaPorcentajeRenglones[i].compareTo(IVA_21) == 0) {
                iva_21_netoFactura =  iva_21_netoFactura.add(cantidades[i].multiply(ivaNetoRenglones[i]
                        .subtract(ivaNetoRenglones[i].multiply(descuentoPorcentaje.divide(CIEN, 15, RoundingMode.HALF_UP)))
                        .add(ivaNetoRenglones[i].multiply(recargoPorcentaje.divide(CIEN, 15, RoundingMode.HALF_UP)))));
            }
        }
        BigDecimal subTotalBruto = subTotal.add(recargo_neto).subtract(descuento_neto).subtract(iva_105_netoFactura.add(iva_21_netoFactura));
        BigDecimal total = subTotalBruto.add(iva_105_netoFactura).add(iva_21_netoFactura);
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
        uri = apiPrefix + "/productos/disponibilidad-stock?idProducto=" + productoUno.getId_Producto() + "," + productoDos.getId_Producto() + "&cantidad=5,4";
        Assert.assertTrue(restTemplate.exchange(uri, HttpMethod.GET, null, new ParameterizedTypeReference<Map<Double, Producto>>() {}).getBody().isEmpty());
        assertTrue("El saldo de la cuenta corriente no es el esperado", 
                restTemplate.getForObject(apiPrefix + "/cuentas-corrientes/clientes/1/saldo", BigDecimal.class)
        .compareTo(new BigDecimal("-5992.5")) == 0);
        List<FacturaVenta> facturasRecuperadas = restTemplate
                .exchange(apiPrefix + "/facturas/venta/busqueda/criteria?idEmpresa=1&tipoFactura=B&nroSerie=0&nroFactura=1", HttpMethod.GET, null,
                        new ParameterizedTypeReference<PaginaRespuestaRest<FacturaVenta>>() {
                })
                .getBody().getContent();
        ReciboDTO r = new ReciboDTO();
        r.setMonto(5992.5);
        restTemplate.postForObject(apiPrefix + "/recibos/clientes?"
                + "idUsuario=1&idEmpresa=1&idCliente=1&idFormaDePago=1", r, Recibo.class);
        assertTrue("El saldo de la cuenta corriente no es el esperado", 
                restTemplate.getForObject(apiPrefix + "/cuentas-corrientes/clientes/1/saldo", BigDecimal.class)
        .compareTo(BigDecimal.ZERO) == 0);
        NotaDebitoDTO notaDebito = new NotaDebitoDTO();
        notaDebito.setCliente(cliente);
        notaDebito.setEmpresa(empresa);
        List<RenglonNotaDebito> renglonesCalculados = Arrays.asList(restTemplate.getForObject(apiPrefix + "/notas/renglon/debito/recibo/1?monto=100&ivaPorcentaje=21", RenglonNotaDebito[].class));
        notaDebito.setRenglonesNotaDebito(renglonesCalculados);
        notaDebito.setIva105Neto(BigDecimal.ZERO);
        notaDebito.setIva21Neto(new BigDecimal("21"));
        notaDebito.setMontoNoGravado(new BigDecimal("5992.5"));
        notaDebito.setMotivo("Test alta nota debito - Cheque rechazado");
        notaDebito.setSubTotalBruto(new BigDecimal("100"));
        notaDebito.setTotal(new BigDecimal("6113.5"));
        notaDebito.setUsuario(credencial);
        notaDebito.setFacturaVenta(null);
        restTemplate.postForObject(apiPrefix + "/notas/debito/empresa/1/cliente/1/usuario/1/recibo/1", notaDebito, NotaDebito.class);
        restTemplate.getForObject(apiPrefix + "/notas/1/reporte", byte[].class);
        assertTrue("El saldo de la cuenta corriente no es el esperado", 
                restTemplate.getForObject(apiPrefix + "/cuentas-corrientes/clientes/1/saldo", BigDecimal.class)
        .compareTo(new BigDecimal("-6113.5")) == 0);
        r = new ReciboDTO();
        r.setMonto(6113.5);
        restTemplate.postForObject(apiPrefix + "/recibos/clientes?"
                + "idUsuario=1&idEmpresa=1&idCliente=1&idFormaDePago=" + formaDePago.getId_FormaDePago(), r, Recibo.class);
        assertTrue("El saldo de la cuenta corriente no es el esperado", 
                restTemplate.getForObject(apiPrefix + "/cuentas-corrientes/clientes/1/saldo", BigDecimal.class)
        .compareTo(BigDecimal.ZERO) == 0);
        List<RenglonNotaCredito> renglonesNotaCredito = Arrays.asList(restTemplate.getForObject(apiPrefix + "/notas/renglon/credito/producto?"
                + "tipoDeComprobante=" + facturasRecuperadas.get(0).getTipoComprobante().name()
                + "&cantidad=5&idRenglonFactura=1", RenglonNotaCredito[].class));
        NotaCreditoDTO notaCredito = new NotaCreditoDTO();
        notaCredito.setRenglonesNotaCredito(renglonesNotaCredito);
        notaCredito.setSubTotal(restTemplate.getForObject(apiPrefix + "/notas/credito/sub-total?importe="
                + renglonesNotaCredito.get(0).getImporteNeto(), BigDecimal.class));
        notaCredito.setRecargoPorcentaje(facturasRecuperadas.get(0).getRecargo_porcentaje());
        notaCredito.setRecargoNeto(restTemplate.getForObject(apiPrefix + "/notas/credito/recargo-neto?subTotal="
                + notaCredito.getSubTotal()
                + "&recargoPorcentaje=" + notaCredito.getRecargoPorcentaje(), BigDecimal.class));
        notaCredito.setDescuentoPorcentaje(facturasRecuperadas.get(0).getDescuento_porcentaje());
        notaCredito.setDescuentoNeto(restTemplate.getForObject(apiPrefix + "/notas/credito/descuento-neto?subTotal="
                + notaCredito.getSubTotal()
                + "&descuentoPorcentaje=" + notaCredito.getDescuentoPorcentaje(), BigDecimal.class));
        notaCredito.setIva21Neto(restTemplate.getForObject(apiPrefix + "/notas/credito/iva-neto?"
                + "tipoDeComprobante=" + facturasRecuperadas.get(0).getTipoComprobante().name()
                + "&cantidades=" + renglonesNotaCredito.get(0).getCantidad()
                + "&ivaPorcentajeRenglones=" + renglonesNotaCredito.get(0).getIvaPorcentaje()
                + "&ivaNetoRenglones=" + renglonesNotaCredito.get(0).getIvaNeto()
                + "&ivaPorcentaje=21"
                + "&descuentoPorcentaje=" + facturasRecuperadas.get(0).getDescuento_porcentaje()
                + "&recargoPorcentaje=" + facturasRecuperadas.get(0).getRecargo_porcentaje(), BigDecimal.class));
        notaCredito.setIva105Neto(restTemplate.getForObject(apiPrefix + "/notas/credito/iva-neto?"
                + "tipoDeComprobante=" + facturasRecuperadas.get(0).getTipoComprobante().name()
                + "&cantidades=" + renglonesNotaCredito.get(0).getCantidad()
                + "&ivaPorcentajeRenglones=" + renglonesNotaCredito.get(0).getIvaPorcentaje()
                + "&ivaNetoRenglones=" + renglonesNotaCredito.get(0).getIvaNeto()
                + "&ivaPorcentaje=10.5"
                + "&descuentoPorcentaje=" + facturasRecuperadas.get(0).getDescuento_porcentaje()
                + "&recargoPorcentaje=" + facturasRecuperadas.get(0).getRecargo_porcentaje(), BigDecimal.class));
        notaCredito.setSubTotalBruto(restTemplate.getForObject(apiPrefix + "/notas/credito/sub-total-bruto?"
                + "tipoDeComprobante=" + facturasRecuperadas.get(0).getTipoComprobante().name()
                + "&subTotal=" + notaCredito.getSubTotal()
                + "&recargoNeto=" + notaCredito.getRecargoNeto()
                + "&descuentoNeto=" + notaCredito.getDescuentoNeto()
                + "&iva21Neto=" + notaCredito.getIva21Neto()
                + "&iva105Neto=" + notaCredito.getIva105Neto(), BigDecimal.class));
        notaCredito.setTotal(restTemplate.getForObject(apiPrefix + "/notas/credito/total?subTotalBruto=" + notaCredito.getSubTotalBruto()
                + "&iva21Neto=" + notaCredito.getIva21Neto()
                + "&iva105Neto=" + notaCredito.getIva105Neto(), BigDecimal.class));
        restTemplate.postForObject(apiPrefix + "/notas/credito/empresa/1/cliente/1/usuario/1/factura/1?modificarStock=true", notaCredito, NotaCredito.class);
        restTemplate.getForObject(apiPrefix + "/notas/2/reporte", byte[].class);
        uri = apiPrefix + "/productos/disponibilidad-stock?idProducto=" + productoUno.getId_Producto() + "," + productoDos.getId_Producto() + "&cantidad=10,4";
        Assert.assertTrue(restTemplate.exchange(uri, HttpMethod.GET, null, new ParameterizedTypeReference<Map<Double, Producto>>() {}).getBody().isEmpty());
        assertTrue("El saldo de la cuenta corriente no es el esperado", 
                restTemplate.getForObject(apiPrefix + "/cuentas-corrientes/clientes/1/saldo", BigDecimal.class)
        .compareTo(new BigDecimal("4114")) == 0);
        List<RenglonCuentaCorriente> renglonesCuentaCorriente = restTemplate
                .exchange(apiPrefix + "/cuentas-corrientes/1/renglones"
                        + "?pagina=" + 0 + "&tamanio=" + 50, HttpMethod.GET, null,
                        new ParameterizedTypeReference<PaginaRespuestaRest<RenglonCuentaCorriente>>() {
                }).getBody().getContent();
        assertTrue("El saldo parcial del renglon no es el esperado", renglonesCuentaCorriente.get(0).getSaldo() == 4114);
        assertTrue("El saldo parcial del renglon no es el esperado", renglonesCuentaCorriente.get(1).getSaldo() == 0);
        assertTrue("El saldo parcial del renglon no es el esperado", renglonesCuentaCorriente.get(2).getSaldo() == -6113.5);
        assertTrue("El saldo parcial del renglon no es el esperado", renglonesCuentaCorriente.get(3).getSaldo() == 0);
        assertTrue("El saldo parcial del renglon no es el esperado", renglonesCuentaCorriente.get(4).getSaldo() == -5992.5);
    }

    @Test
    public void testCuentaCorrienteProveedor() {
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
        restTemplate.postForObject(apiPrefix + "/clientes", cliente, Cliente.class);
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
        Assert.assertTrue(restTemplate.exchange(uri, HttpMethod.GET, null, new ParameterizedTypeReference<Map<BigDecimal, Producto>>() {}).getBody().isEmpty());
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
        BigDecimal descuentoPorcentaje = new BigDecimal("25");
        BigDecimal recargoPorcentaje = BigDecimal.TEN;
        BigDecimal descuento_neto = subTotal.multiply(descuentoPorcentaje).divide(CIEN, 15, RoundingMode.HALF_UP);
        BigDecimal recargo_neto = subTotal.multiply(recargoPorcentaje).divide(CIEN, 15, RoundingMode.HALF_UP);
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
        BigDecimal subTotalBruto = subTotal.add(recargo_neto).subtract(descuento_neto).subtract(iva_105_netoFactura.add(iva_21_netoFactura));
        BigDecimal total = subTotalBruto.add(iva_105_netoFactura).add(iva_21_netoFactura);
        FacturaCompraDTO facturaCompraB = new FacturaCompraDTO();
        facturaCompraB.setFecha(new Date());
        facturaCompraB.setTipoComprobante(TipoDeComprobante.FACTURA_B);
        facturaCompraB.setRenglones(renglones);
        facturaCompraB.setSubTotal(subTotal);
        facturaCompraB.setRecargo_porcentaje(recargoPorcentaje);
        facturaCompraB.setRecargo_neto(recargo_neto);
        facturaCompraB.setDescuento_porcentaje(descuentoPorcentaje);
        facturaCompraB.setDescuento_neto(descuento_neto);
        facturaCompraB.setSubTotal_bruto(subTotalBruto);
        facturaCompraB.setIva_105_neto(iva_105_netoFactura);
        facturaCompraB.setIva_21_neto(iva_21_netoFactura);
        facturaCompraB.setTotal(total);
        restTemplate.postForObject(apiPrefix + "/facturas/compra?"
                + "idProveedor=" + proveedor.getId_Proveedor()
                + "&idEmpresa=" + empresa.getId_Empresa()
                + "&idTransportista=" + transportista.getId_Transportista(), facturaCompraB, FacturaCompraDTO[].class);
        uri = apiPrefix + "/productos/disponibilidad-stock?idProducto=" + productoUno.getId_Producto() + "," + productoDos.getId_Producto() + "&cantidad=15,8";
        Assert.assertTrue(restTemplate.exchange(uri, HttpMethod.GET, null, new ParameterizedTypeReference<Map<Double, Producto>>() {}).getBody().isEmpty());
        assertTrue("El saldo de la cuenta corriente no es el esperado", 
                restTemplate.getForObject(apiPrefix + "/cuentas-corrientes/proveedores/1/saldo", BigDecimal.class)
        .compareTo(new BigDecimal("-5992.5")) == 0);
        ReciboDTO r = new ReciboDTO();
        r.setMonto(5992.5);
        restTemplate.postForObject(apiPrefix + "/recibos/proveedores?"
                + "idUsuario=1&idEmpresa=1&idProveedor=1&idFormaDePago=1", r, Recibo.class);
        restTemplate.exchange(apiPrefix + "/facturas/compra/busqueda/criteria?idEmpresa=1&tipoFactura=B", HttpMethod.GET, null,
                new ParameterizedTypeReference<PaginaRespuestaRest<FacturaCompraDTO>>() {
        })
                .getBody().getContent();
        assertTrue("El saldo de la cuenta corriente no es el esperado",
                restTemplate.getForObject(apiPrefix + "/cuentas-corrientes/proveedores/1/saldo", BigDecimal.class)
                        .compareTo(BigDecimal.ZERO) == 0);
        restTemplate.delete(apiPrefix + "/recibos/1");
        restTemplate.exchange(apiPrefix + "/facturas/compra/busqueda/criteria?idEmpresa=1&tipoFactura=B", HttpMethod.GET, null,
                new ParameterizedTypeReference<PaginaRespuestaRest<FacturaCompraDTO>>() {
        })
                .getBody().getContent();
        assertTrue("El saldo de la cuenta corriente no es el esperado",
                restTemplate.getForObject(apiPrefix + "/cuentas-corrientes/proveedores/1/saldo", BigDecimal.class)
                        .compareTo(new BigDecimal("-5992.5")) == 0);
        r = new ReciboDTO();
        r.setMonto(4992.5);
        restTemplate.postForObject(apiPrefix + "/recibos/proveedores?"
                + "idUsuario=1&idEmpresa=1&idProveedor=1&idFormaDePago=1", r, Recibo.class);
        restTemplate.exchange(apiPrefix + "/facturas/compra/busqueda/criteria?idEmpresa=1&tipoFactura=B", HttpMethod.GET, null,
                new ParameterizedTypeReference<PaginaRespuestaRest<FacturaCompraDTO>>() {
        })
                .getBody().getContent();
        assertTrue("El saldo de la cuenta corriente no es el esperado",
                restTemplate.getForObject(apiPrefix + "/cuentas-corrientes/proveedores/1/saldo", BigDecimal.class)
                        .compareTo(new BigDecimal("-1000")) == 0);
        r = new ReciboDTO();
        r.setMonto(2000);
        restTemplate.postForObject(apiPrefix + "/recibos/proveedores?"
                + "idUsuario=1&idEmpresa=1&idProveedor=1&idFormaDePago=1", r, Recibo.class);
        restTemplate.exchange(apiPrefix + "/facturas/compra/busqueda/criteria?idEmpresa=1&tipoFactura=B", HttpMethod.GET, null,
                new ParameterizedTypeReference<PaginaRespuestaRest<FacturaCompraDTO>>() {
        })
                .getBody().getContent();
        assertTrue("El saldo de la cuenta corriente no es el esperado",
                restTemplate.getForObject(apiPrefix + "/cuentas-corrientes/proveedores/1/saldo", BigDecimal.class)
        .compareTo(new BigDecimal("1000")) == 0);
        restTemplate.delete(apiPrefix + "/facturas?idFactura=1");
        assertTrue("El saldo de la cuenta corriente no es el esperado", 
                restTemplate.getForObject(apiPrefix + "/cuentas-corrientes/proveedores/1/saldo", BigDecimal.class)
        .compareTo(new BigDecimal("6992.5")) == 0);
        uri = apiPrefix + "/productos/disponibilidad-stock?idProducto=" + productoUno.getId_Producto() + "," + productoDos.getId_Producto() + "&cantidad=10,6";
        Assert.assertTrue(restTemplate.exchange(uri, HttpMethod.GET, null, new ParameterizedTypeReference<Map<Double, Producto>>() {}).getBody().isEmpty());
        List<RenglonCuentaCorriente> renglonesCuentaCorriente = restTemplate
                .exchange(apiPrefix + "/cuentas-corrientes/2/renglones"
                        + "?pagina=" + 0 + "&tamanio=" + 50, HttpMethod.GET, null,
                        new ParameterizedTypeReference<PaginaRespuestaRest<RenglonCuentaCorriente>>() {
                }).getBody().getContent();
        assertTrue("El saldo parcial del renglon no es el esperado", renglonesCuentaCorriente.get(0).getSaldo() == 6992.5);
        assertTrue("El saldo parcial del renglon no es el esperado", renglonesCuentaCorriente.get(1).getSaldo() == 4992.5);
    }
    
    @Test
    public void testRecibo() {
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
        BigDecimal descuentoPorcentaje = new BigDecimal("25");
        BigDecimal recargoPorcentaje = BigDecimal.TEN;
        BigDecimal descuento_neto = subTotal.multiply(descuentoPorcentaje).divide(CIEN, 15, RoundingMode.HALF_UP);
        BigDecimal recargo_neto = subTotal.multiply(recargoPorcentaje).divide(CIEN, 15, RoundingMode.HALF_UP);
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
        BigDecimal subTotalBruto = subTotal.add(recargo_neto).subtract(descuento_neto).subtract(iva_105_netoFactura.add(iva_21_netoFactura));
        BigDecimal total = subTotalBruto.add(iva_105_netoFactura).add(iva_21_netoFactura);
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
        assertEquals(-5992.5, restTemplate.getForObject(apiPrefix + "/cuentas-corrientes/clientes/1/saldo", Double.class), 0);
        ProductoDTO productoTres = new ProductoBuilder()
                .withCodigo("3")
                .withDescripcion("tres")
                .withCantidad(new BigDecimal("30"))
                .withVentaMinima(BigDecimal.ONE)
                .withPrecioVentaPublico(new BigDecimal("2000"))
                .withIva_porcentaje(new BigDecimal("21.0"))
                .withIva_neto(new BigDecimal("420"))
                .withPrecioLista(new BigDecimal("2420"))
                .build();
        ProductoDTO productoCuatro = new ProductoBuilder()
                .withCodigo("4")
                .withDescripcion("cuatro")
                .withCantidad(new BigDecimal("12"))
                .withVentaMinima(BigDecimal.ONE)
                .withPrecioVentaPublico(new BigDecimal("2000"))
                .withIva_porcentaje(new BigDecimal("10.5"))
                .withIva_neto(new BigDecimal("210"))
                .withPrecioLista(new BigDecimal("1210"))
                .build();
        productoTres = restTemplate.postForObject(apiPrefix + "/productos?idMedida=" + medida.getId_Medida() + "&idRubro=" + rubro.getId_Rubro()
                + "&idProveedor=" + proveedor.getId_Proveedor() + "&idEmpresa=" + empresa.getId_Empresa(),
                productoTres, ProductoDTO.class);
        productoCuatro = restTemplate.postForObject(apiPrefix + "/productos?idMedida=" + medida.getId_Medida() + "&idRubro=" + rubro.getId_Rubro()
                + "&idProveedor=" + proveedor.getId_Proveedor() + "&idEmpresa=" + empresa.getId_Empresa(),
                productoCuatro, ProductoDTO.class);
        uri = apiPrefix + "/productos/disponibilidad-stock?idProducto=" + productoTres.getId_Producto() + "," + productoCuatro.getId_Producto() + "&cantidad=5,2";
        Assert.assertTrue(restTemplate.exchange(uri, HttpMethod.GET, null, new ParameterizedTypeReference<Map<Double, Producto>>() {}).getBody().isEmpty());
        RenglonFactura renglonTres = restTemplate.getForObject(apiPrefix + "/facturas/renglon?"
                + "idProducto=" + productoTres.getId_Producto()
                + "&tipoDeComprobante=" + TipoDeComprobante.FACTURA_X
                + "&movimiento=" + Movimiento.VENTA
                + "&cantidad=5"
                + "&descuentoPorcentaje=20",
                RenglonFactura.class);
        RenglonFactura renglonCuatro = restTemplate.getForObject(apiPrefix + "/facturas/renglon?"
                + "idProducto=" + productoCuatro.getId_Producto()
                + "&tipoDeComprobante=" + TipoDeComprobante.FACTURA_X
                + "&movimiento=" + Movimiento.VENTA
                + "&cantidad=2"
                + "&descuentoPorcentaje=0",
                RenglonFactura.class);
        renglones = new ArrayList<>();
        renglones.add(renglonTres);
        renglones.add(renglonCuatro);
        size = renglones.size();
        cantidades = new BigDecimal[size];
        ivaPorcentajeRenglones = new BigDecimal[size];
        ivaNetoRenglones = new BigDecimal[size];
        indice = 0;
        subTotal = BigDecimal.ZERO;
        for (RenglonFactura renglon : renglones) {
            subTotal = subTotal.add(renglon.getImporte());
            cantidades[indice] = renglon.getCantidad();
            ivaPorcentajeRenglones[indice] = renglon.getIva_porcentaje();
            ivaNetoRenglones[indice] = renglon.getIva_neto();
            indice++;
        }
        descuentoPorcentaje = new BigDecimal("25");
        recargoPorcentaje = BigDecimal.TEN;
        descuento_neto = subTotal.multiply(descuentoPorcentaje).divide(CIEN, 15, RoundingMode.HALF_UP);
        recargo_neto = subTotal.multiply(recargoPorcentaje).divide(CIEN, 15, RoundingMode.HALF_UP);
        indice = cantidades.length;
        iva_105_netoFactura = BigDecimal.ZERO;
        iva_21_netoFactura = BigDecimal.ZERO;
        for (int i = 0; i < indice; i++) {
            if (ivaPorcentajeRenglones[i].compareTo(IVA_105) == 0) {
                iva_105_netoFactura = iva_105_netoFactura.add(cantidades[i].multiply(ivaNetoRenglones[i].subtract(ivaNetoRenglones[i].multiply(descuentoPorcentaje.divide(CIEN, 15, RoundingMode.HALF_UP)))
                        .add(ivaNetoRenglones[i].multiply(recargoPorcentaje.divide(CIEN, 15, RoundingMode.HALF_UP)))));
            } else if (ivaPorcentajeRenglones[i].compareTo(IVA_21) == 0) {
                iva_21_netoFactura = iva_21_netoFactura.add(cantidades[i].multiply(ivaNetoRenglones[i].subtract(ivaNetoRenglones[i].multiply(descuentoPorcentaje.divide(CIEN, 15, RoundingMode.HALF_UP)))
                        .add(ivaNetoRenglones[i].multiply(recargoPorcentaje.divide(CIEN, 15, RoundingMode.HALF_UP)))));
            }
        }
        subTotalBruto = subTotal.add(recargo_neto).subtract(descuento_neto).subtract(iva_105_netoFactura.add(iva_21_netoFactura));
        total = subTotalBruto.add(iva_105_netoFactura).add(iva_21_netoFactura);
        FacturaVentaDTO facturaVentaX = new FacturaVentaDTO();
        facturaVentaX.setTipoComprobante(TipoDeComprobante.FACTURA_X);
        facturaVentaX.setRenglones(renglones);
        facturaVentaX.setSubTotal(subTotal);
        facturaVentaX.setRecargo_porcentaje(recargoPorcentaje);
        facturaVentaX.setRecargo_neto(recargo_neto);
        facturaVentaX.setDescuento_porcentaje(descuentoPorcentaje);
        facturaVentaX.setDescuento_neto(descuento_neto);
        facturaVentaX.setSubTotal_bruto(subTotalBruto);
        facturaVentaX.setIva_105_neto(iva_105_netoFactura);
        facturaVentaX.setIva_21_neto(iva_21_netoFactura);
        facturaVentaX.setTotal(total);
        restTemplate.postForObject(apiPrefix + "/facturas/venta?"
                + "idCliente=" + cliente.getId_Cliente()
                + "&idEmpresa=" + empresa.getId_Empresa()
                + "&idUsuario=" + credencial.getId_Usuario()
                + "&idTransportista=" + transportista.getId_Transportista(), facturaVentaX, FacturaVenta[].class);
        assertEquals(-16192.5, restTemplate.getForObject(apiPrefix + "/cuentas-corrientes/clientes/1/saldo", Double.class), 0);
        facturaVentaX = restTemplate.getForObject(apiPrefix + "/facturas/2", FacturaVentaDTO.class);
        assertTrue("El total no es el esperado", facturaVentaX.getTotal().compareTo(new BigDecimal("10200")) == 0);
        ReciboDTO r = new ReciboDTO();
        restTemplate.postForObject(apiPrefix + "/recibos/clientes?"
                + "idUsuario=1&idEmpresa=1&idCliente=1&idFormaDePago=1", r, ReciboDTO.class);
        assertEquals(-1192.5, restTemplate.getForObject(apiPrefix + "/cuentas-corrientes/clientes/1/saldo", Double.class), 0);
        restTemplate.getForObject(apiPrefix + "/recibos/1/reporte", byte[].class);
        facturaVentaX = restTemplate.getForObject(apiPrefix + "/facturas/2", FacturaVentaDTO.class);
        assertEquals(TipoDeComprobante.FACTURA_X, facturaVentaX.getTipoComprobante());
        facturaVentaB = restTemplate.getForObject(apiPrefix + "/facturas/1", FacturaVentaDTO.class);
        assertEquals(TipoDeComprobante.FACTURA_B, facturaVentaB.getTipoComprobante());
        r = new ReciboDTO();
        r.setMonto(2192.5);
        restTemplate.postForObject(apiPrefix + "/recibos/clientes?"
                + "idUsuario=1&idEmpresa=1&idCliente=1&idFormaDePago=1", r, ReciboDTO.class);
        assertEquals(1000, restTemplate.getForObject(apiPrefix + "/cuentas-corrientes/clientes/1/saldo", Double.class), 0);
        facturaVentaB = restTemplate.getForObject(apiPrefix + "/facturas/1", FacturaVentaDTO.class);
        assertEquals(TipoDeComprobante.FACTURA_B, facturaVentaB.getTipoComprobante());
        NotaDebitoDTO notaDebito = new NotaDebitoDTO();
        notaDebito.setCAE(0L);
        notaDebito.setCliente(cliente);
        notaDebito.setEmpresa(empresa);
        notaDebito.setFecha(new Date());
        List<RenglonNotaDebito> renglonesCalculados = Arrays.asList(restTemplate.getForObject(apiPrefix + "/notas/renglon/debito/recibo/2?monto=1000&ivaPorcentaje=21", RenglonNotaDebito[].class));
        notaDebito.setRenglonesNotaDebito(renglonesCalculados);
        notaDebito.setIva105Neto(BigDecimal.ZERO);
        notaDebito.setIva21Neto(new BigDecimal("210"));
        notaDebito.setMontoNoGravado(new BigDecimal("2192.5"));
        notaDebito.setMotivo("Test alta nota debito - Cheque rechazado");
        notaDebito.setSubTotalBruto(new BigDecimal("1000"));
        notaDebito.setTotal(new BigDecimal("3402.5"));
        notaDebito.setUsuario(credencial);
        notaDebito.setFacturaVenta(null);
        restTemplate.postForObject(apiPrefix + "/notas/debito/empresa/1/cliente/1/usuario/1/recibo/2", notaDebito, NotaDebito.class);
        assertEquals(-2402.5, restTemplate.getForObject(apiPrefix + "/cuentas-corrientes/clientes/1/saldo", Double.class), 0);
        restTemplate.delete(apiPrefix + "/facturas?idFactura=1");
        assertEquals(3590, restTemplate.getForObject(apiPrefix + "/cuentas-corrientes/clientes/1/saldo", Double.class), 0);
        restTemplate.delete(apiPrefix + "/notas?idsNota=1");
        assertEquals(6992.5, restTemplate.getForObject(apiPrefix + "/cuentas-corrientes/clientes/1/saldo", Double.class), 0);
        List<RenglonCuentaCorriente> renglonesCuentaCorriente = restTemplate
                .exchange(apiPrefix + "/cuentas-corrientes/1/renglones"
                        + "?pagina=" + 0 + "&tamanio=" + 50, HttpMethod.GET, null,
                        new ParameterizedTypeReference<PaginaRespuestaRest<RenglonCuentaCorriente>>() {
                }).getBody().getContent();
        assertTrue("El saldo parcial del renglon no es el esperado", renglonesCuentaCorriente.get(0).getSaldo() == 6992.5);
        assertTrue("El saldo parcial del renglon no es el esperado", renglonesCuentaCorriente.get(1).getSaldo() == 4800);
        assertTrue("El saldo parcial del renglon no es el esperado", renglonesCuentaCorriente.get(2).getSaldo() == -10200);
    }

}
