package sic.service.impl;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mercadopago.MercadoPago;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.Payment;
import com.mercadopago.resources.Preference;
import com.mercadopago.resources.Refund;
import com.mercadopago.resources.datastructures.preference.BackUrls;
import com.mercadopago.resources.datastructures.preference.Item;
import com.mercadopago.resources.datastructures.preference.PaymentMethods;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import sic.exception.BusinessServiceException;
import sic.exception.ServiceException;
import sic.modelo.*;
import sic.util.EncryptUtils;
import sic.modelo.dto.MercadoPagoPreferenceDTO;
import sic.modelo.dto.NuevaNotaDebitoDeReciboDTO;
import sic.modelo.dto.NuevaOrdenDePagoDTO;
import sic.service.*;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Validated
public class MercadoPagoServiceImpl implements IMercadoPagoService {

  @Value("${SIC_MERCADOPAGO_ACCESS_TOKEN}")
  private String mercadoPagoAccesToken;

  private final IReciboService reciboService;
  private final IFormaDePagoService formaDePagoService;
  private final IClienteService clienteService;
  private final INotaService notaService;
  private final ISucursalService sucursalService;
  private final ICarritoCompraService carritoCompraService;
  private final IUsuarioService usuarioService;
  private final IPedidoService pedidoService;
  private final EncryptUtils encryptUtils;
  private static final String MENSAJE_PAGO_NO_SOPORTADO = "mensaje_pago_no_soportado";
  private static final Long ID_SUCURSAL_DEFAULT = 1L;
  private static final String STRING_ID_USUARIO = "idUsuario";
  private static final String[] MEDIO_DE_PAGO_NO_PERMITIDOS =
          new String[] {"rapipago", "pagofacil", "bapropagos", "cobroexpress", "cargavirtual", "redlink"};
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final MessageSource messageSource;

  @Autowired
  public MercadoPagoServiceImpl(
      IReciboService reciboService,
      IFormaDePagoService formaDePagoService,
      IClienteService clienteService,
      INotaService notaService,
      ISucursalService sucursalService,
      ICarritoCompraService carritoCompraService,
      IUsuarioService usuarioService,
      IPedidoService pedidoService,
      EncryptUtils encryptUtils,
      MessageSource messageSource) {
    this.reciboService = reciboService;
    this.formaDePagoService = formaDePagoService;
    this.clienteService = clienteService;
    this.notaService = notaService;
    this.sucursalService = sucursalService;
    this.carritoCompraService = carritoCompraService;
    this.usuarioService = usuarioService;
    this.pedidoService = pedidoService;
    this.encryptUtils = encryptUtils;
    this.messageSource = messageSource;
  }

  @Override
  public MercadoPagoPreferenceDTO crearNuevaPreference(
      long idUsuario, @Valid NuevaOrdenDePagoDTO nuevaOrdenDeCompra, String origin) {
    if (nuevaOrdenDeCompra.getIdSucursal() == null) {
      if (nuevaOrdenDeCompra.getTipoDeEnvio().equals(TipoDeEnvio.RETIRO_EN_SUCURSAL)) {
        throw new BusinessServiceException(
            messageSource.getMessage(
                "mensaje_preference_retiro_sucursal_no_seleccionada", null, Locale.getDefault()));

      } else {
        nuevaOrdenDeCompra.setIdSucursal(ID_SUCURSAL_DEFAULT);
      }
    }
    Cliente clienteDeUsuario = clienteService.getClientePorIdUsuario(idUsuario);
    if (clienteDeUsuario.getEmail() == null || clienteDeUsuario.getEmail().isEmpty()) {
      throw new BusinessServiceException(
          messageSource.getMessage("mensaje_preference_cliente_sin_email", null, Locale.getDefault()));
    }
    MercadoPago.SDK.configure(mercadoPagoAccesToken);
    Preference preference = new Preference();
    String json = "";
    BackUrls backUrls = null;
    String title = "";
    float monto;
    switch (nuevaOrdenDeCompra.getMovimiento()) {
      case PEDIDO:
        if (nuevaOrdenDeCompra.getTipoDeEnvio() == null) {
          throw new BusinessServiceException(
              messageSource.getMessage(
                  "mensaje_preference_sin_tipo_de_envio", null, Locale.getDefault()));
        }
        monto = carritoCompraService.calcularTotal(idUsuario).floatValue();
        title = "Pedido de (" + clienteDeUsuario.getNroCliente() + ") " + clienteDeUsuario.getNombreFiscal();
        json =
            "{ \""
                + STRING_ID_USUARIO
                + "\": "
                + idUsuario
                + " , \"idSucursal\": "
                + nuevaOrdenDeCompra.getIdSucursal()
                + " , \"tipoDeEnvio\": "
                + nuevaOrdenDeCompra.getTipoDeEnvio()
                + " , \"movimiento\": "
                + Movimiento.PEDIDO
                + "}";
        backUrls =
            new BackUrls(
                origin + "/checkout/aprobado",
                origin + "/checkout/pendiente",
                origin + "/carrito-compra");
        break;
      case DEPOSITO:
        if (nuevaOrdenDeCompra.getMonto() == null) {
          throw new BusinessServiceException(
              messageSource.getMessage(
                  "mensaje_preference_deposito_sin_monto", null, Locale.getDefault()));
        }
        monto = nuevaOrdenDeCompra.getMonto().floatValue();
        title = "Deposito de (" + clienteDeUsuario.getNroCliente() + ") " + clienteDeUsuario.getNombreFiscal();
        json =
            "{ \""
                + STRING_ID_USUARIO
                + "\": "
                + idUsuario
                + " , \"idSucursal\": "
                + nuevaOrdenDeCompra.getIdSucursal()
                + " , \"movimiento\": "
                + Movimiento.DEPOSITO
                + "}";
        String urlDeposito = origin + "/perfil";
        backUrls = new BackUrls(urlDeposito, urlDeposito, urlDeposito);
        break;
      default:
        throw new BusinessServiceException(
            messageSource.getMessage("mensaje_preference_tipo_de_movimiento_no_soportado", null, Locale.getDefault()));
    }
    JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject();
    try {
      preference.setExternalReference(
          encryptUtils.encryptWhitAES(jsonObject.toString()));
    } catch (GeneralSecurityException e) {
      throw new ServiceException(
          messageSource.getMessage("mensaje_error_al_encriptar", null, Locale.getDefault()), e);
    }
    Item item = new Item();
    item.setTitle(title).setQuantity(1).setUnitPrice(monto);
    com.mercadopago.resources.datastructures.preference.Payer payer =
        new com.mercadopago.resources.datastructures.preference.Payer();
    payer.setEmail(clienteDeUsuario.getEmail());
    preference.setPayer(payer);
    preference.appendItem(item);
    preference.setBackUrls(backUrls);
    preference.setBinaryMode(true);
    if (!clienteDeUsuario.isPuedeComprarAPlazo()) {
      PaymentMethods paymentMethods = new PaymentMethods();
      paymentMethods.setExcludedPaymentMethods(MEDIO_DE_PAGO_NO_PERMITIDOS);
      preference.setPaymentMethods(paymentMethods);
    }
    try {
      preference = preference.save();
    } catch (MPException ex) {
      this.logExceptionMercadoPago(ex);
    }
    return new MercadoPagoPreferenceDTO(preference.getId(), preference.getInitPoint());
  }

  @Override
  public void crearComprobantePorNotificacion(String idPayment) {
    Payment payment;
    try {
      MercadoPago.SDK.configure(mercadoPagoAccesToken);
      payment = Payment.findById(idPayment);
      if (payment.getId() != null && payment.getExternalReference() != null) {
        Optional<Recibo> reciboMP = reciboService.getReciboPorIdMercadoPago(idPayment);
        JsonObject convertedObject =
            new Gson()
                .fromJson(
                    encryptUtils.decryptWhitAES(payment.getExternalReference()), JsonObject.class);
        JsonElement idUsuario = convertedObject.get(STRING_ID_USUARIO);
        if (idUsuario == null) {
          throw new BusinessServiceException(
              messageSource.getMessage(
                  "mensaje_preference_tipo_de_movimiento_no_soportado", null, Locale.getDefault()));
        }
        JsonElement idSucursal = convertedObject.get("idSucursal");
        if (idSucursal == null) {
          throw new BusinessServiceException(
              messageSource.getMessage(
                  "mensaje_preference_tipo_de_movimiento_no_soportado", null, Locale.getDefault()));
        }
        Cliente cliente =
            clienteService.getClientePorIdUsuario(Long.parseLong(idUsuario.getAsString()));
        Sucursal sucursal =
            sucursalService.getSucursalPorId(Long.parseLong(idSucursal.getAsString()));
        Movimiento movimiento = Movimiento.valueOf(convertedObject.get("movimiento").getAsString());
        TipoDeEnvio tipoDeEnvio;
        switch (payment.getStatus()) {
          case approved:
            if (reciboMP.isPresent()) {
              logger.warn(
                  messageSource.getMessage(
                      "mensaje_recibo_de_pago_no_existente",
                      new Object[] {payment.getId()},
                      Locale.getDefault()));
            } else {
              switch (movimiento) {
                case PEDIDO:
                  tipoDeEnvio =
                      TipoDeEnvio.valueOf(convertedObject.get("tipoDeEnvio").getAsString());
                  Pedido pedidoDePayment = pedidoService.getPedidoPorIdPayment(payment.getId());
                  if (pedidoDePayment == null) {
                    this.crearPedidoDelCarrito(
                        Long.parseLong(convertedObject.get(STRING_ID_USUARIO).getAsString()),
                        sucursal,
                        cliente,
                        tipoDeEnvio,
                        payment.getId());
                  } else {
                    logger.warn(
                        messageSource.getMessage(
                            "mensaje_pedido_payment_ya_existente",
                            new Object[] {pedidoDePayment},
                            Locale.getDefault()));
                  }
                  this.crearReciboDePago(payment, cliente.getCredencial(), cliente, sucursal);
                  break;
                case DEPOSITO:
                  this.crearReciboDePago(payment, cliente.getCredencial(), cliente, sucursal);
                  break;
                default:
                  throw new BusinessServiceException(
                      messageSource.getMessage(
                          "mensaje_preference_tipo_de_movimiento_no_soportado", null, Locale.getDefault()));
              }
            }
            break;
          case pending:
            Pedido pedidoDePayment = pedidoService.getPedidoPorIdPayment(payment.getId());
            if (pedidoDePayment == null && movimiento == Movimiento.PEDIDO) {
              tipoDeEnvio = TipoDeEnvio.valueOf(convertedObject.get("tipoDeEnvio").getAsString());
              this.crearPedidoDelCarrito(
                  Long.parseLong(convertedObject.get(STRING_ID_USUARIO).getAsString()),
                  sucursal,
                  cliente,
                  tipoDeEnvio,
                  payment.getId());
            }
            break;
          case refunded:
            if (!reciboMP.isPresent())
              throw new EntityNotFoundException(
                  messageSource.getMessage(
                      "mensaje_recibo_no_existente", null, Locale.getDefault()));
            if (!notaService.existsNotaDebitoPorRecibo(reciboMP.get())) {
              this.crearNotaDebito(
                  reciboMP.get().getIdRecibo(),
                  reciboMP.get().getIdCliente(),
                  reciboMP.get().getSucursal().getIdSucursal(),
                  cliente.getCredencial());
            } else {
              logger.warn(
                  messageSource.getMessage(
                      "mensaje_nota_pago_existente",
                      new Object[] {payment.getId()},
                      Locale.getDefault()));
            }
            break;
          case rejected:
            logger.error(
                messageSource.getMessage(
                    "mensaje_pago_rechazado", new Object[] {payment}, Locale.getDefault()));
            break;
          default:
            logger.warn(
                messageSource.getMessage(
                    "mensaje_pago_status_no_soportado",
                    new Object[] {payment.getId()},
                    Locale.getDefault()));
            messageSource.getMessage(MENSAJE_PAGO_NO_SOPORTADO, null, Locale.getDefault());
        }
      } else {
        throw new BusinessServiceException(
            messageSource.getMessage(MENSAJE_PAGO_NO_SOPORTADO, null, Locale.getDefault()));
      }
    } catch (MPException ex) {
      this.logExceptionMercadoPago(ex);
    } catch (GeneralSecurityException e) {
      throw new BusinessServiceException(
          messageSource.getMessage("mensaje_error_al_desencriptar", null, Locale.getDefault()));
    }
  }

  @Override
  public void devolverPago(String idPayment) {
    try {
      Payment payment = Payment.findById(idPayment);
      if (payment.getStatus().equals(Payment.Status.approved)) {
        MercadoPago.SDK.configure(mercadoPagoAccesToken);
        Refund refund = new Refund();
        refund.setPaymentId(idPayment);
        refund.save();
      }
    } catch (MPException ex) {
      throw new BusinessServiceException(
          messageSource.getMessage(
              "mensaje_pago_error", new Object[] {ex.getMessage()}, Locale.getDefault()));
    }
  }

  private void crearReciboDePago(
      Payment payment, Usuario usuario, Cliente cliente, Sucursal sucursal) {
    switch (payment.getStatus()) {
      case approved:
        logger.warn(
            messageSource.getMessage(
                "mensaje_pago_aprobado", new Object[] {payment}, Locale.getDefault()));
        Recibo nuevoRecibo = new Recibo();
        nuevoRecibo.setSucursal(sucursal);
        nuevoRecibo.setFormaDePago(
            formaDePagoService.getFormaDePagoPorNombre(FormaDePagoEnum.MERCADO_PAGO));
        nuevoRecibo.setUsuario(usuario);
        nuevoRecibo.setCliente(cliente);
        nuevoRecibo.setFecha(LocalDateTime.now());
        nuevoRecibo.setConcepto("Pago en MercadoPago (" + payment.getPaymentMethodId() + ")");
        nuevoRecibo.setMonto(new BigDecimal(Float.toString(payment.getTransactionAmount())));
        nuevoRecibo.setIdPagoMercadoPago(payment.getId());
        reciboService.guardar(nuevoRecibo);
        break;
      case pending:
        if (payment.getStatusDetail().equals("pending_waiting_payment")) {
          logger.warn(
              messageSource.getMessage(
                  "mensaje_pago_pendiente", new Object[] {payment}, Locale.getDefault()));
        } else {
          logger.warn(
              messageSource.getMessage(
                  "mensaje_pago_no_aprobado", new Object[] {payment}, Locale.getDefault()));
          this.procesarMensajeNoAprobado(payment);
        }
        break;
      default:
        logger.warn(
            messageSource.getMessage(
                "mensaje_pago_no_aprobado", new Object[] {payment}, Locale.getDefault()));
        this.procesarMensajeNoAprobado(payment);
    }
  }

  private void crearNotaDebito(
      Long idRecibo, Long idCliente, Long idSucursal, Usuario usuarioCliente) {
    NuevaNotaDebitoDeReciboDTO nuevaNotaDebitoDeReciboDTO =
        NuevaNotaDebitoDeReciboDTO.builder()
            .idRecibo(idRecibo)
            .gastoAdministrativo(BigDecimal.ZERO)
            .motivo("Devolución de pago por MercadoPago")
            .tipoDeComprobante(notaService.getTipoNotaDebitoCliente(idCliente, idSucursal).get(0))
            .build();
    NotaDebito notaGuardada =
        notaService.guardarNotaDebito(
            notaService.calcularNotaDebitoConRecibo(nuevaNotaDebitoDeReciboDTO, usuarioCliente));
    notaService.autorizarNota(notaGuardada);
  }

  private void crearPedidoDelCarrito(
      Long idUsuario,
      Sucursal sucursal,
      Cliente cliente,
      TipoDeEnvio tipoDeEnvio,
      String idPayment) {
    Usuario usuario = usuarioService.getUsuarioNoEliminadoPorId(idUsuario);
    List<ItemCarritoCompra> items = carritoCompraService.getItemsDelCarritoPorUsuario(usuario);
    Pedido pedido = new Pedido();
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
    pedido.setIdPayment(idPayment);
    pedidoService.guardar(pedido);
    carritoCompraService.eliminarTodosLosItemsDelUsuario(usuario.getIdUsuario());
  }

  @Override
  public void logExceptionMercadoPago(MPException ex) {
    throw new BusinessServiceException(
        messageSource.getMessage(
            "mensaje_pago_error", new Object[] {ex.getMessage()}, Locale.getDefault()));
  }

  private void procesarMensajeNoAprobado(Payment payment) {
    if (payment.getStatusDetail() != null) {
      switch (payment.getStatusDetail()) {
        case "cc_rejected_card_disabled":
        case "cc_rejected_insufficient_amount":
        case "cc_rejected_other_reason":
          throw new BusinessServiceException(
              messageSource.getMessage(
                  payment.getStatusDetail(),
                  new Object[] {payment.getPaymentMethodId()},
                  Locale.getDefault()));
        case "cc_rejected_call_for_authorize":
          throw new BusinessServiceException(
              messageSource.getMessage(
                  payment.getStatusDetail(),
                  new Object[] {payment.getPaymentMethodId(), payment.getTransactionAmount()},
                  Locale.getDefault()));
        case "cc_rejected_invalid_installments":
          throw new BusinessServiceException(
              messageSource.getMessage(
                  payment.getStatusDetail(),
                  new Object[] {payment.getPaymentMethodId(), payment.getInstallments()},
                  Locale.getDefault()));
        default:
          throw new BusinessServiceException(
              messageSource.getMessage(payment.getStatusDetail(), null, Locale.getDefault()));
      }
    } else {
      throw new BusinessServiceException(payment.getLastApiResponse().getStringResponse());
    }
  }
}
