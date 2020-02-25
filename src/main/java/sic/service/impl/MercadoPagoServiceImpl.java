package sic.service.impl;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mercadopago.MercadoPago;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.Payment;
import com.mercadopago.resources.Preference;
import com.mercadopago.resources.Refund;
import com.mercadopago.resources.datastructures.preference.Item;
import com.mercadopago.resources.datastructures.payment.Payer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import sic.exception.BusinessServiceException;
import sic.modelo.*;
import sic.modelo.dto.NuevaNotaDebitoDeReciboDTO;
import sic.modelo.dto.NuevoPagoMercadoPagoDTO;
import sic.service.*;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

@Service
@Validated
public class MercadoPagoServiceImpl implements IMercadoPagoService {

  @Value("${SIC_MERCADOPAGO_ACCESS_TOKEN}")
  private String mercadoPagoAccesToken;

  private static final String[] pagosEnEfectivoPermitidos =
      new String[] {"pagofacil", "rapipago", "cobroexpress", "cargavirtual"};
  private final IReciboService reciboService;
  private final IFormaDePagoService formaDePagoService;
  private final IClienteService clienteService;
  private final INotaService notaService;
  private final ISucursalService sucursalService;
  private static final String MENSAJE_PAGO_NO_SOPORTADO = "mensaje_pago_no_soportado";
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final MessageSource messageSource;

  @Autowired
  public MercadoPagoServiceImpl(
      IReciboService reciboService,
      IFormaDePagoService formaDePagoService,
      IClienteService clienteService,
      INotaService notaService,
      ISucursalService sucursalService,
      MessageSource messageSource) {
    this.reciboService = reciboService;
    this.formaDePagoService = formaDePagoService;
    this.clienteService = clienteService;
    this.notaService = notaService;
    this.sucursalService = sucursalService;
    this.messageSource = messageSource;
  }

  @Override
  public String crearNuevoPago(@Valid NuevoPagoMercadoPagoDTO nuevoPagoMercadoPagoDTO)
      throws MPException {
    Cliente cliente =
        clienteService.getClienteNoEliminadoPorId(nuevoPagoMercadoPagoDTO.getIdCliente());
    this.validarOperacion(nuevoPagoMercadoPagoDTO, cliente);
    MercadoPago.SDK.configure(mercadoPagoAccesToken);
    Payment payment = new Payment();
    payment.setDescription(
        "("
            + cliente.getNroCliente()
            + ")"
            + " "
            + cliente.getNombreFiscal()
            + (cliente.getNombreFantasia() != null ? cliente.getNombreFantasia() : ""));
    String json =
        "{ \"idCliente\": "
            + nuevoPagoMercadoPagoDTO.getIdCliente()
            + " , \"idSucursal\": "
            + nuevoPagoMercadoPagoDTO.getIdSucursal()
            + "}";
    JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject();
    payment.setExternalReference(jsonObject.toString());
    Payer payer = new Payer();
    payer.setEmail(cliente.getEmail());
    payment.setPayer(payer);
    if (nuevoPagoMercadoPagoDTO.getToken() != null
        && !nuevoPagoMercadoPagoDTO.getToken().isEmpty()) {
      payment
          .setTransactionAmount(nuevoPagoMercadoPagoDTO.getMonto())
          .setToken(nuevoPagoMercadoPagoDTO.getToken())
          .setInstallments(nuevoPagoMercadoPagoDTO.getInstallments())
          .setIssuerId(nuevoPagoMercadoPagoDTO.getIssuerId())
          .setPaymentMethodId(nuevoPagoMercadoPagoDTO.getPaymentMethodId())
          .setBinaryMode(true);
    } else if (Arrays.asList(pagosEnEfectivoPermitidos)
        .contains(nuevoPagoMercadoPagoDTO.getPaymentMethodId())) {
      payment
          .setTransactionAmount(nuevoPagoMercadoPagoDTO.getMonto())
          .setPaymentMethodId(nuevoPagoMercadoPagoDTO.getPaymentMethodId());
    } else {
      throw new BusinessServiceException(
          messageSource.getMessage(MENSAJE_PAGO_NO_SOPORTADO, null, Locale.getDefault()));
    }
    Payment pago = payment.save();
    if (pago.getStatus() == Payment.Status.rejected) {
      this.procesarMensajeNoAprobado(payment);
    }
    return pago.getId();
  }

  @Override
  public Preference crearNuevaPreferencia(
      String nombreProducto, int cantidad, float precioUnitario) {
    Preference preference = new Preference();
    Item item = new Item();
    item.setTitle(nombreProducto).setQuantity(cantidad).setUnitPrice(precioUnitario);
    preference.appendItem(item);
    try {
      preference = preference.save();
    } catch (MPException ex) {
      this.logExceptionMercadoPago(ex);
    }
    return preference;
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
            new Gson().fromJson(payment.getExternalReference(), JsonObject.class);
        Cliente cliente =
            clienteService.getClienteNoEliminadoPorId(
                Long.parseLong(convertedObject.get("idCliente").getAsString()));
        Sucursal sucursal =
            sucursalService.getSucursalPorId(
                Long.parseLong(convertedObject.get("idSucursal").getAsString()));
        switch (payment.getStatus()) {
          case approved:
            if (reciboMP.isPresent()) {
              logger.warn("El recibo del pago nro {} ya existe.", payment.getId());
            } else {
              this.crearReciboDePago(payment, cliente.getCredencial(), cliente, sucursal);
            }
            break;
          case refunded:
            if (!reciboMP.isPresent())
              throw new EntityNotFoundException(
                  messageSource.getMessage(
                      "mensaje_recibo_no_existente", null, Locale.getDefault()));
            if (!notaService.existsNotaDebitoPorRecibo(reciboMP.get())) {
              NuevaNotaDebitoDeReciboDTO nuevaNotaDebitoDeReciboDTO =
                  NuevaNotaDebitoDeReciboDTO.builder()
                      .idRecibo(reciboMP.get().getIdRecibo())
                      .gastoAdministrativo(BigDecimal.ZERO)
                      .motivo("Devolución de pago por MercadoPago")
                      .tipoDeComprobante(
                          notaService
                              .getTipoNotaDebitoCliente(
                                  reciboMP.get().getIdCliente(),
                                  reciboMP.get().getSucursal().getIdSucursal())
                              .get(0))
                      .build();
              NotaDebito notaGuardada =
                  notaService.guardarNotaDebito(
                      notaService.calcularNotaDebitoConRecibo(
                          nuevaNotaDebitoDeReciboDTO, cliente.getCredencial()));
              notaService.autorizarNota(notaGuardada);
            } else {
              logger.warn("La nota del pago nro {} ya existe.", payment.getId());
            }
            break;
          default:
            logger.warn("El status del pago nro {} no es soportado.", payment.getId());
            messageSource.getMessage(MENSAJE_PAGO_NO_SOPORTADO, null, Locale.getDefault());
        }
      } else {
        throw new BusinessServiceException(
            messageSource.getMessage(MENSAJE_PAGO_NO_SOPORTADO, null, Locale.getDefault()));
      }
    } catch (MPException ex) {
      this.logExceptionMercadoPago(ex);
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
      logger.warn("Ocurrió un error con MercadoPago: {}", ex.getMessage());
      throw new BusinessServiceException(
          messageSource.getMessage("mensaje_pago_error", null, Locale.getDefault()));
    }
  }

  private void crearReciboDePago(
      Payment payment, Usuario usuario, Cliente cliente, Sucursal sucursal) {
    switch (payment.getStatus()) {
      case approved:
        logger.warn("El pago de mercadopago {} se aprobó correctamente.", payment);
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
          logger.warn("El pago {} está pendiente", payment.getId());
        } else {
          logger.warn("El pago {} no fué aprobado", payment.getId());
          this.procesarMensajeNoAprobado(payment);
        }
        break;
      default:
        logger.warn("El pago {} no fué aprobado", payment.getId());
        this.procesarMensajeNoAprobado(payment);
    }
  }

  @Override
  public void logExceptionMercadoPago(MPException ex) {
    logger.warn("Ocurrió un error con MercadoPago: {}", ex.getMessage());
    throw new BusinessServiceException(
        messageSource.getMessage("mensaje_pago_error", null, Locale.getDefault()));
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

  private void validarOperacion(NuevoPagoMercadoPagoDTO nuevoPagoMercadoPagoDTO, Cliente cliente) {
    if (cliente.getEmail() == null || cliente.getEmail().isEmpty()) {
      throw new BusinessServiceException(
          messageSource.getMessage("mensaje_pago_cliente_sin_email", null, Locale.getDefault()));
    }
    if (nuevoPagoMercadoPagoDTO.getInstallments() == null) {
      nuevoPagoMercadoPagoDTO.setInstallments(1);
    }
    if (nuevoPagoMercadoPagoDTO.getToken() != null
        && !nuevoPagoMercadoPagoDTO.getToken().isEmpty()
        && (nuevoPagoMercadoPagoDTO.getIssuerId() == null
            || nuevoPagoMercadoPagoDTO.getIssuerId().isEmpty())) {
      throw new BusinessServiceException(
          messageSource.getMessage("mensaje_pago_sin_issuer_id", null, Locale.getDefault()));
    }
  }
}
