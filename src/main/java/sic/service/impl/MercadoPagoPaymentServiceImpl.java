package sic.service.impl;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.payment.PaymentRefundClient;
import com.mercadopago.client.preference.*;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.preference.Preference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import sic.exception.BusinessServiceException;
import sic.exception.ServiceException;
import sic.modelo.*;
import sic.modelo.dto.*;
import sic.util.CustomValidator;
import sic.util.EncryptUtils;
import sic.service.*;
import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Arrays;

@Service
public class MercadoPagoPaymentServiceImpl implements IPaymentService {

  @Value("${MERCADOPAGO_ACCESS_TOKEN}")
  private String mercadoPagoAccessToken;

  private final IReciboService reciboService;
  private final IClienteService clienteService;
  private final INotaService notaService;
  private final ISucursalService sucursalService;
  private final ICarritoCompraService carritoCompraService;
  private final IUsuarioService usuarioService;
  private final IPedidoService pedidoService;
  private final IProductoService productoService;
  private final EncryptUtils encryptUtils;
  private static final String MENSAJE_PAGO_NO_SOPORTADO = "mensaje_pago_no_soportado";
  private static final String MENSAJE_PREFERENCE_TIPO_MOVIMIENTO_NO_SOPORTADO = "mensaje_preference_tipo_de_movimiento_no_soportado";
  private static final String MENSAJE_PAGO_ERROR = "mensaje_pago_error";
  private static final String MENSAJE_SERVICIO_NO_CONFIGURADO = "El servicio de Mercado Pago no se encuentra configurado";
  private static final String STRING_ID_USUARIO = "idUsuario";
  private static final String[] MEDIOS_DE_PAGO_NO_PERMITIDOS = new String[] {
          "rapipago", "pagofacil", "bapropagos", "cobroexpress", "cargavirtual", "redlink"
  };
  private static final String APPROVED = "approved";
  private static final String REFUNDED = "refunded";
  private static final String REJECTED = "rejected";
  private static final String PENDING = "pending";
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final MessageSource messageSource;
  private final CustomValidator customValidator;

  @Autowired
  public MercadoPagoPaymentServiceImpl(
    IReciboService reciboService,
    IClienteService clienteService,
    INotaService notaService,
    ISucursalService sucursalService,
    ICarritoCompraService carritoCompraService,
    IUsuarioService usuarioService,
    IPedidoService pedidoService,
    IProductoService productoService,
    EncryptUtils encryptUtils,
    MessageSource messageSource,
    CustomValidator customValidator) {
    this.reciboService = reciboService;
    this.clienteService = clienteService;
    this.notaService = notaService;
    this.sucursalService = sucursalService;
    this.carritoCompraService = carritoCompraService;
    this.usuarioService = usuarioService;
    this.pedidoService = pedidoService;
    this.productoService = productoService;
    this.encryptUtils = encryptUtils;
    this.messageSource = messageSource;
    this.customValidator = customValidator;
  }

  @Override
  public boolean isServicioConfigurado() {
    return mercadoPagoAccessToken != null && !mercadoPagoAccessToken.isEmpty();
  }

  @Override
  public List<String> getNuevaPreferenceParams(long idUsuario, NuevaOrdenDePagoDTO nuevaOrdenDeCompra, String origin) {
    if (!isServicioConfigurado()) throw new ServiceException(MENSAJE_SERVICIO_NO_CONFIGURADO);
    customValidator.validar(nuevaOrdenDeCompra);
    var sucursal = sucursalService.getSucursalPorId(nuevaOrdenDeCompra.getIdSucursal());
    var usuario = usuarioService.getUsuarioNoEliminadoPorId(idUsuario);
    var items = carritoCompraService.getItemsDelCarritoPorUsuario(usuario);
    var clienteDeUsuario = clienteService.getClientePorIdUsuario(idUsuario);
    if (clienteDeUsuario.getEmail() == null || clienteDeUsuario.getEmail().isEmpty()) {
      throw new BusinessServiceException(
          messageSource.getMessage(
              "mensaje_preference_cliente_sin_email", null, Locale.getDefault()));
    }
    MercadoPagoConfig.setAccessToken(mercadoPagoAccessToken);
    Preference preference;
    var client = new PreferenceClient();
    PreferenceBackUrlsRequest backUrls;
    String stringJson;
    String title;
    BigDecimal monto;
    Pedido pedido = null;
    var casePedido = false;
    switch (nuevaOrdenDeCompra.getMovimiento()) {
      case PEDIDO -> {
        if (this.verificarStockItemsDelCarrito(items)) {
        pedido =
                this.crearPedidoPorPreference(
                        sucursal, usuario, clienteDeUsuario, items, nuevaOrdenDeCompra.getTipoDeEnvio());
        if (nuevaOrdenDeCompra.getTipoDeEnvio() == null) {
          throw new BusinessServiceException(
                  messageSource.getMessage(
                          "mensaje_preference_sin_tipo_de_envio", null, Locale.getDefault()));
        }
        casePedido = true;
        monto = carritoCompraService.calcularTotal(idUsuario);
        title =
                "Pedido de ("
                        + clienteDeUsuario.getNroCliente()
                        + ") "
                        + clienteDeUsuario.getNombreFiscal();
        stringJson =
                "{ \""
                        + STRING_ID_USUARIO
                        + "\": "
                        + idUsuario
                        + " , \"idSucursal\": "
                        + sucursal.getIdSucursal()
                        + " , \"tipoDeEnvio\": "
                        + nuevaOrdenDeCompra.getTipoDeEnvio()
                        + " , \"movimiento\": "
                        + Movimiento.PEDIDO
                        + " , \"idPedido\": "
                        + pedido.getIdPedido()
                        + "}";
        backUrls = PreferenceBackUrlsRequest.builder()
                .success(origin + "/checkout/aprobado")
                .pending(origin + "/checkout/pendiente")
                .failure(origin + "/carrito-compra")
                .build();
        } else {
          throw new BusinessServiceException(
                  messageSource.getMessage("mensaje_preference_sin_stock", null, Locale.getDefault()));
        }
      }
      case DEPOSITO -> {
        if (nuevaOrdenDeCompra.getMonto() == null) {
          throw new BusinessServiceException(
                  messageSource.getMessage(
                          "mensaje_preference_deposito_sin_monto", null, Locale.getDefault()));
        }
        monto = nuevaOrdenDeCompra.getMonto();
        title =
                "Deposito de ("
                        + clienteDeUsuario.getNroCliente()
                        + ") "
                        + clienteDeUsuario.getNombreFiscal();
        stringJson =
                "{ \""
                        + STRING_ID_USUARIO
                        + "\": "
                        + idUsuario
                        + " , \"idSucursal\": "
                        + sucursal.getIdSucursal()
                        + " , \"movimiento\": "
                        + Movimiento.DEPOSITO
                        + "}";
        String urlDeposito = origin + "/perfil";
        backUrls = PreferenceBackUrlsRequest.builder()
                .success(urlDeposito)
                .pending(urlDeposito)
                .failure(urlDeposito)
                .build();
      }
      default -> throw new BusinessServiceException(
              messageSource.getMessage(MENSAJE_PREFERENCE_TIPO_MOVIMIENTO_NO_SOPORTADO, null, Locale.getDefault()));
    }
    var jsonObject = JsonParser.parseString(stringJson).getAsJsonObject();
    try {
      stringJson = encryptUtils.encryptWhitAES(jsonObject.toString());
    } catch (GeneralSecurityException e) {
      throw new ServiceException(
          messageSource.getMessage("mensaje_error_al_encriptar", null, Locale.getDefault()), e);
    }
    var preferenceItems = new ArrayList<PreferenceItemRequest>();
    preferenceItems.add(PreferenceItemRequest.builder()
            .title(title).quantity(1).unitPrice(monto).build());
    var preferencePayerRequest = PreferencePayerRequest.builder()
            .email(clienteDeUsuario.getEmail())
            .build();
    var excludedPaymentMethods = new ArrayList<PreferencePaymentMethodRequest>();
    if (!clienteDeUsuario.isPuedeComprarAPlazo()) {
      Arrays.stream(MEDIOS_DE_PAGO_NO_PERMITIDOS).toList().forEach(
              medioDePago -> excludedPaymentMethods.add(PreferencePaymentMethodRequest.builder().id(medioDePago).build())
      );
    }
    var paymentMethods =
            PreferencePaymentMethodsRequest.builder()
                    .excludedPaymentMethods(excludedPaymentMethods)
                    .installments(12)
                    .build();
    try {
      PreferenceRequest request = casePedido ? PreferenceRequest.builder()
              .externalReference(stringJson)
              .additionalInfo(stringJson)
              .backUrls(backUrls)
              .items(preferenceItems)
              .payer(preferencePayerRequest)
              .paymentMethods(paymentMethods)
              .binaryMode(true)
              .expires(true)
              .dateOfExpiration(pedido.getFechaVencimiento().plusSeconds(30).atOffset(OffsetDateTime.now().getOffset()))
              .build() :
              PreferenceRequest.builder()
              .externalReference(stringJson)
              .backUrls(backUrls)
              .items(preferenceItems)
              .payer(preferencePayerRequest)
              .paymentMethods(paymentMethods)
              .binaryMode(true)
              .build();
      preference = client.create(request);
      if (nuevaOrdenDeCompra.getMovimiento() == Movimiento.PEDIDO && pedido != null) {
        carritoCompraService.eliminarTodosLosItemsDelUsuario(idUsuario);
      }
    } catch (MPException | MPApiException ex) {
      if (nuevaOrdenDeCompra.getMovimiento() == Movimiento.PEDIDO && pedido != null) {
        pedidoService.eliminar(pedido.getIdPedido());
      }
      throw new BusinessServiceException(
              messageSource.getMessage(MENSAJE_PAGO_ERROR, new Object[]{ex.getMessage()}, Locale.getDefault()));
    }
    return Arrays.asList(preference.getId(), preference.getInitPoint());
  }

  @Override
  public void crearComprobantePorNotificacion(long idPayment) {
    if (!isServicioConfigurado()) throw new ServiceException(MENSAJE_SERVICIO_NO_CONFIGURADO);
    Payment payment;
    try {
      MercadoPagoConfig.setAccessToken(mercadoPagoAccessToken);
      var paymentClient = new PaymentClient();
      payment = paymentClient.get(idPayment);
      if (payment.getId() != null && payment.getExternalReference() != null) {
        var reciboMP = reciboService.getReciboPorIdMercadoPago(idPayment);
        var convertedObject =
            new Gson()
                .fromJson(
                    encryptUtils.decryptWhitAES(payment.getExternalReference()), JsonObject.class);
        var idUsuario = convertedObject.get(STRING_ID_USUARIO);
        if (idUsuario == null) {
          throw new BusinessServiceException(
              messageSource.getMessage(MENSAJE_PREFERENCE_TIPO_MOVIMIENTO_NO_SOPORTADO, null, Locale.getDefault()));
        }
        var idSucursal = convertedObject.get("idSucursal");
        if (idSucursal == null) {
          throw new BusinessServiceException(
              messageSource.getMessage(MENSAJE_PREFERENCE_TIPO_MOVIMIENTO_NO_SOPORTADO, null, Locale.getDefault()));
        }
        var cliente = clienteService.getClientePorIdUsuario(Long.parseLong(idUsuario.getAsString()));
        var sucursal = sucursalService.getSucursalPorId(Long.parseLong(idSucursal.getAsString()));
        var movimiento = Movimiento.valueOf(convertedObject.get("movimiento").getAsString());
        long idPedido;
        switch (payment.getStatus()) {
          case APPROVED -> {
            if (reciboMP.isPresent()) {
              logger.warn(messageSource.getMessage(
                              "mensaje_recibo_de_pago_ya_existente",
                              new Object[]{payment.getId()},
                              Locale.getDefault()));
            } else {
              switch (movimiento) {
                case PEDIDO -> {
                  idPedido = Long.parseLong(String.valueOf(convertedObject.get("idPedido")));
                  pedidoService.cambiarFechaDeVencimiento(idPedido);
                  this.crearReciboDePago(payment, cliente.getCredencial(), cliente, sucursal);
                }
                case DEPOSITO -> this.crearReciboDePago(payment, cliente.getCredencial(), cliente, sucursal);
                default -> throw new BusinessServiceException(
                        messageSource.getMessage(MENSAJE_PREFERENCE_TIPO_MOVIMIENTO_NO_SOPORTADO, null, Locale.getDefault()));
              }
            }
          }
          case REFUNDED -> {
            if (reciboMP.isEmpty())
              throw new EntityNotFoundException(messageSource.getMessage(
                              "mensaje_recibo_no_existente", null, Locale.getDefault()));
            if (!notaService.existsNotaDebitoPorRecibo(reciboMP.get())) {
              this.crearNotaDebito(
                      reciboMP.get().getIdRecibo(),
                      reciboMP.get().getIdCliente(),
                      reciboMP.get().getSucursal().getIdSucursal(),
                      cliente.getCredencial());
            } else {
              logger.warn(messageSource.getMessage(
                              "mensaje_nota_pago_existente",
                              new Object[]{payment.getId()},
                              Locale.getDefault()));
            }
          }
          case REJECTED -> logger.error(messageSource.getMessage(
                          "mensaje_pago_rechazado", new Object[]{payment}, Locale.getDefault()));
          default -> {
            logger.warn(messageSource.getMessage(
                            "mensaje_pago_status_no_soportado",
                            new Object[]{payment.getId()},
                            Locale.getDefault()));
            messageSource.getMessage(MENSAJE_PAGO_NO_SOPORTADO, null, Locale.getDefault());
          }
        }
      } else {
        throw new BusinessServiceException(
            messageSource.getMessage(MENSAJE_PAGO_NO_SOPORTADO, null, Locale.getDefault()));
      }
    } catch (MPException | MPApiException ex) {
      throw new BusinessServiceException(
              messageSource.getMessage(MENSAJE_PAGO_ERROR, new Object[]{ex.getMessage()}, Locale.getDefault()));
    }
    catch (GeneralSecurityException e) {
      throw new ServiceException(
          messageSource.getMessage("mensaje_error_al_desencriptar", null, Locale.getDefault()), e);
    }
  }

  @Override
  public void devolverPago(long idPayment) {
    if (!isServicioConfigurado()) throw new ServiceException(MENSAJE_SERVICIO_NO_CONFIGURADO);
    try {
      var paymentClient = new PaymentClient();
      var payment = paymentClient.get(idPayment);
      if (payment.getStatus().equals(APPROVED)) {
        MercadoPagoConfig.setAccessToken(mercadoPagoAccessToken);
        PaymentRefundClient refund = new PaymentRefundClient();
        refund.refund(idPayment);
      }
    } catch (MPApiException | MPException ex) {
      throw new BusinessServiceException(
          messageSource.getMessage(MENSAJE_PAGO_ERROR, new Object[] {ex.getMessage()}, Locale.getDefault()));
    }
  }

  private void crearReciboDePago(Payment payment, Usuario usuario, Cliente cliente, Sucursal sucursal) {
    switch (payment.getStatus()) {
      case APPROVED -> {
        logger.warn(
                messageSource.getMessage(
                        "mensaje_pago_aprobado", new Object[]{payment}, Locale.getDefault()));
        reciboService.guardar(
                reciboService.construirReciboPorPayment(sucursal, usuario, cliente, payment));
      }
      case PENDING -> {
        if (payment.getStatusDetail().equals("pending_waiting_payment")) {
          logger.warn(
                  messageSource.getMessage(
                          "mensaje_pago_pendiente", new Object[]{payment}, Locale.getDefault()));
        } else {
          logger.warn(
                  messageSource.getMessage(
                          "mensaje_pago_no_aprobado", new Object[]{payment}, Locale.getDefault()));
          this.procesarMensajeNoAprobado(payment);
        }
      }
      default -> {
        logger.warn(
                messageSource.getMessage(
                        "mensaje_pago_no_aprobado", new Object[]{payment}, Locale.getDefault()));
        this.procesarMensajeNoAprobado(payment);
      }
    }
  }

  private void crearNotaDebito(Long idRecibo, Long idCliente, Long idSucursal, Usuario usuarioCliente) {
    var nuevaNotaDebitoDeReciboDTO =
        NuevaNotaDebitoDeReciboDTO.builder()
            .idRecibo(idRecibo)
            .gastoAdministrativo(BigDecimal.ZERO)
            .motivo("Devolución de pago por MercadoPago")
            .tipoDeComprobante(notaService.getTipoNotaDebitoCliente(idCliente, idSucursal).get(0))
            .build();
    var notaGuardada = notaService.guardarNotaDebito(
            notaService.calcularNotaDebitoConRecibo(nuevaNotaDebitoDeReciboDTO, usuarioCliente));
    var facturaElectronicaHabilitada = notaGuardada.getSucursal().getConfiguracionSucursal().isFacturaElectronicaHabilitada();
    if (facturaElectronicaHabilitada) notaService.autorizarNota(notaGuardada);
  }

  private Pedido crearPedidoPorPreference(
      Sucursal sucursal,
      Usuario usuario,
      Cliente cliente,
      List<ItemCarritoCompra> items,
      TipoDeEnvio tipoDeEnvio) {
    var pedido = new Pedido();
    pedido.setRecargoPorcentaje(BigDecimal.ZERO);
    pedido.setDescuentoPorcentaje(BigDecimal.ZERO);
    pedido.setSucursal(sucursal);
    pedido.setUsuario(usuario);
    pedido.setCliente(cliente);
    List<RenglonPedido> renglonesPedido = new ArrayList<>();
    items.forEach(
        i ->
            renglonesPedido.add(
                pedidoService.calcularRenglonPedido(
                    i.getProducto().getIdProducto(), i.getCantidad())));
    pedido.setRenglones(renglonesPedido);
    pedido.setTipoDeEnvio(tipoDeEnvio);
    pedido.setFecha(LocalDateTime.now());
    return pedidoService.guardar(pedido, Collections.emptyList());
  }

  private boolean verificarStockItemsDelCarrito(List<ItemCarritoCompra> items) {
    var idProducto = new long[items.size()];
    var cantidad = new BigDecimal[items.size()];
    int i = 0;
    for (ItemCarritoCompra item : items) {
      idProducto[i] = item.getProducto().getIdProducto();
      cantidad[i] = item.getCantidad();
      i++;
    }
    var productosParaVerificarStockDTO =
        ProductosParaVerificarStockDTO.builder()
                .idProducto(idProducto)
                .cantidad(cantidad)
                .idSucursal(sucursalService.getSucursalPredeterminada().getIdSucursal())
                .build();
    return productoService.getProductosSinStockDisponible(productosParaVerificarStockDTO).isEmpty();
  }

  private void procesarMensajeNoAprobado(Payment payment) {
    if (payment.getStatusDetail() != null) {
      switch (payment.getStatusDetail()) {
        case "cc_rejected_card_disabled", "cc_rejected_insufficient_amount", "cc_rejected_other_reason" ->
                throw new BusinessServiceException(
                        messageSource.getMessage(
                                payment.getStatusDetail(),
                                new Object[]{payment.getPaymentMethodId()},
                                Locale.getDefault()));
        case "cc_rejected_call_for_authorize" -> throw new BusinessServiceException(
                messageSource.getMessage(
                        payment.getStatusDetail(),
                        new Object[]{payment.getPaymentMethodId(), payment.getTransactionAmount()},
                        Locale.getDefault()));
        case "cc_rejected_invalid_installments" -> throw new BusinessServiceException(
                messageSource.getMessage(
                        payment.getStatusDetail(),
                        new Object[]{payment.getPaymentMethodId(), payment.getInstallments()},
                        Locale.getDefault()));
        default -> throw new BusinessServiceException(
                messageSource.getMessage(payment.getStatusDetail(), null, Locale.getDefault()));
      }
    } else {
      throw new BusinessServiceException(payment.getStatusDetail());
    }
  }
}
