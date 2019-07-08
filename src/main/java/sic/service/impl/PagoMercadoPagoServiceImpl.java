package sic.service.impl;

import com.mercadopago.MercadoPago;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.Payment;
import com.mercadopago.resources.datastructures.payment.Payer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import sic.exception.BusinessServiceException;
import sic.modelo.Cliente;
import sic.modelo.Recibo;
import sic.modelo.dto.PagoMercadoPagoDTO;
import sic.service.*;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Locale;;

@Service
@Validated
public class PagoMercadoPagoServiceImpl implements IPagoMercadoPagoService {

  @Value("${SIC_MERCADOPAGO_ACCESS_TOKEN}")
  private String mercadoPagoAccesToken;

  private final IReciboService reciboService;
  private final IFormaDePagoService formaDePagoService;
  private final IClienteService clienteService;
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final MessageSource messageSource;

  @Autowired
  public PagoMercadoPagoServiceImpl(
      IReciboService reciboService,
      IFormaDePagoService formaDePagoService,
      IClienteService clienteService,
      MessageSource messageSource) {
    this.reciboService = reciboService;
    this.formaDePagoService = formaDePagoService;
    this.clienteService = clienteService;
    this.messageSource = messageSource;
  }

  @Override
  public Recibo crearNuevoPago(PagoMercadoPagoDTO pagoMercadoPagoDTO) {
    Cliente cliente = clienteService.getClienteNoEliminadoPorId(pagoMercadoPagoDTO.getIdCliente());
    MercadoPago.SDK.configure(mercadoPagoAccesToken);
    Payment payment = new Payment();
    Payer payer = new Payer();
    payer.setFirstName(cliente.getNombreFiscal());
    payer.setLastName(cliente.getNombreFantasia());
    payer.setEmail(cliente.getEmail());
    if (pagoMercadoPagoDTO.getPaymentMethodId() != null
        && pagoMercadoPagoDTO.getPaymentMethodId().isEmpty()) {
      throw new BusinessServiceException(
          messageSource.getMessage(
              "mensaje_pago_sin_payment_method_id", null, Locale.getDefault()));
    }
    if (pagoMercadoPagoDTO.getToken() != null && !pagoMercadoPagoDTO.getToken().isEmpty()) {
      if (pagoMercadoPagoDTO.getIssuerId() != null && pagoMercadoPagoDTO.getIssuerId().isEmpty()) {
        throw new BusinessServiceException(
          messageSource.getMessage(
            "mensaje_pago_sin_issuer_id", null, Locale.getDefault()));
      }
      if (pagoMercadoPagoDTO.getInstallments() != null) {
        pagoMercadoPagoDTO.setInstallments(1);
      }
      payment
          .setTransactionAmount(pagoMercadoPagoDTO.getMonto())
          .setToken(pagoMercadoPagoDTO.getToken())
          .setDescription("Pago a Globo de Oro - Credito")
          .setInstallments(pagoMercadoPagoDTO.getInstallments())
          .setIssuerId(pagoMercadoPagoDTO.getIssuerId())
          .setPaymentMethodId(pagoMercadoPagoDTO.getPaymentMethodId())
          .setBinaryMode(true)
          .setPayer(payer);
    } else if (pagoMercadoPagoDTO.getToken() == null || pagoMercadoPagoDTO.getToken().isEmpty()) {
      payment
          .setTransactionAmount(pagoMercadoPagoDTO.getMonto())
          .setDescription("Pago a Globo de Oro - Debito")
          .setPaymentMethodId(pagoMercadoPagoDTO.getPaymentMethodId())
          .setBinaryMode(true)
          .setPayer(payer);
    } else {
      throw new BusinessServiceException(
          messageSource.getMessage("mensaje_pago_no_soportado", null, Locale.getDefault()));
    }
    Recibo nuevoRecibo = new Recibo();
    try {
      payment = payment.save();
      if (payment.getStatus() == Payment.Status.approved) {
        logger.warn("El pago de mercadopago {} se aprobó correctamente.", payment);
        nuevoRecibo.setEmpresa(cliente.getEmpresa());
        nuevoRecibo.setFormaDePago(formaDePagoService.getFormasDePagoPorId(16));
        nuevoRecibo.setUsuario(cliente.getCredencial());
        nuevoRecibo.setCliente(cliente);
        nuevoRecibo.setFecha(new Date());
        nuevoRecibo.setConcepto("probando pago");
        nuevoRecibo.setMonto(new BigDecimal(Float.toString(pagoMercadoPagoDTO.getMonto())));
        nuevoRecibo.setIdPagoMercadoPago(payment.getId());
        nuevoRecibo = reciboService.guardar(nuevoRecibo);
      } else {
        this.lanzarExcepcionSegunDetalleDePayment(payment);
      }
    } catch (MPException exception) {
      logger.error(exception.toString());
      this.lanzarBussinesExceptionPorExceptionMercadoPago(exception);
    }
    return nuevoRecibo;
  }

  @Override
  public PagoMercadoPagoDTO recuperarPago(String idPago) {
    MercadoPago.SDK.configure(mercadoPagoAccesToken);
    PagoMercadoPagoDTO pagoRecuperado = new PagoMercadoPagoDTO();
    try {
      Payment pagoMP = Payment.findById(idPago);
      Recibo reciboDeMercadoPago = reciboService.getReciboPorIdMercadoPago(idPago);
      pagoRecuperado.setInstallments(pagoMP.getInstallments());
      pagoRecuperado.setIdCliente(reciboDeMercadoPago.getIdCliente());
      pagoRecuperado.setIssuerId(pagoMP.getIssuerId());
      pagoRecuperado.setMonto(reciboDeMercadoPago.getMonto().floatValue());
      pagoRecuperado.setPaymentMethodId(pagoMP.getPaymentMethodId());
    } catch (MPException | NullPointerException e) {
      logger.error(e.toString());
    }
    return pagoRecuperado;
  }

  @Override
  public PagoMercadoPagoDTO devolverPago(String idPago) {
    MercadoPago.SDK.configure(mercadoPagoAccesToken);
    PagoMercadoPagoDTO pagoRecuperado = new PagoMercadoPagoDTO();
    try {
      Payment pagoMP = Payment.findById(idPago);
      pagoMP = pagoMP.refund();
      Recibo reciboDeMercadoPago = reciboService.getReciboPorIdMercadoPago(idPago);
      pagoRecuperado.setInstallments(pagoMP.getInstallments());
      pagoRecuperado.setIdCliente(reciboDeMercadoPago.getIdCliente());
      pagoRecuperado.setIssuerId(pagoMP.getIssuerId());
      pagoRecuperado.setMonto(reciboDeMercadoPago.getMonto().floatValue());
      pagoRecuperado.setPaymentMethodId(pagoMP.getPaymentMethodId());
    } catch (MPException | NullPointerException e) {
      logger.error(e.toString());
    }
    return pagoRecuperado;
  }

  private void lanzarExcepcionSegunDetalleDePayment(Payment payment) {
    switch (payment.getStatusDetail()) {
      case "pending_contingency":
        throw new BusinessServiceException(messageSource.getMessage("pending_contingency", null, Locale.getDefault()));
      case "pending_review_manual":
        throw new BusinessServiceException(messageSource.getMessage("pending_review_manual", null, Locale.getDefault()));
      case "cc_rejected_bad_filled_card_number":
        throw new BusinessServiceException(
          messageSource.getMessage("cc_rejected_bad_filled_card_number", null, Locale.getDefault()));
      case "cc_rejected_bad_filled_date":
        throw new BusinessServiceException(
          messageSource.getMessage("cc_rejected_bad_filled_date", null, Locale.getDefault()));
      case "cc_rejected_bad_filled_other":
        throw new BusinessServiceException(
          messageSource.getMessage("cc_rejected_bad_filled_other", null, Locale.getDefault()));
      case "cc_rejected_bad_filled_security_code":
        throw new BusinessServiceException(
          messageSource.getMessage("cc_rejected_bad_filled_security_code", null, Locale.getDefault()));
      case "cc_rejected_blacklist":
        throw new BusinessServiceException(messageSource.getMessage("cc_rejected_blacklist", null, Locale.getDefault()));
      case "cc_rejected_call_for_authorize":
        throw new BusinessServiceException(
          messageSource.getMessage("cc_rejected_call_for_authorize", null, Locale.getDefault()));
      case "cc_rejected_card_disabled":
        throw new BusinessServiceException(messageSource.getMessage("cc_rejected_card_disabled", null, Locale.getDefault()));
      case "cc_rejected_card_error":
        throw new BusinessServiceException(messageSource.getMessage("cc_rejected_card_error", null, Locale.getDefault()));
      case "cc_rejected_duplicated_payment":
        throw new BusinessServiceException(
          messageSource.getMessage("cc_rejected_duplicated_payment", null, Locale.getDefault()));
      case "cc_rejected_high_risk":
        throw new BusinessServiceException(messageSource.getMessage("cc_rejected_high_risk", null, Locale.getDefault()));
      case "cc_rejected_insufficient_amount":
        throw new BusinessServiceException(
          messageSource.getMessage("cc_rejected_insufficient_amount", null, Locale.getDefault()));
      case "cc_rejected_invalid_installments":
        throw new BusinessServiceException(
            MessageFormat.format(
              messageSource.getMessage("cc_rejected_invalid_installments", null, Locale.getDefault()),
                payment.getInstallments()));
      case "cc_rejected_max_attempts":
        throw new BusinessServiceException(messageSource.getMessage("cc_rejected_max_attempts", null, Locale.getDefault()));
      default:
        throw new BusinessServiceException(messageSource.getMessage("cc_rejected_other_reason", null, Locale.getDefault()));
    }
  }

  private void lanzarBussinesExceptionPorExceptionMercadoPago(MPException exception) {
    switch (exception.getStatusCode()) {
      case 1:
        throw new BusinessServiceException(messageSource.getMessage("error_1", null, Locale.getDefault()));
      case 3:
        throw new BusinessServiceException(messageSource.getMessage("error_3", null, Locale.getDefault()));
      case 5:
        throw new BusinessServiceException(messageSource.getMessage("error_5", null, Locale.getDefault()));
      case 1000:
        throw new BusinessServiceException(messageSource.getMessage("error_1000", null, Locale.getDefault()));
      case 1001:
        throw new BusinessServiceException(messageSource.getMessage("error_1001", null, Locale.getDefault()));
      case 2001:
        throw new BusinessServiceException(messageSource.getMessage("error_2001", null, Locale.getDefault()));
      case 2004:
        throw new BusinessServiceException(messageSource.getMessage("error_2004", null, Locale.getDefault()));
      case 2002:
        throw new BusinessServiceException(messageSource.getMessage("error_2002", null, Locale.getDefault()));
      case 2006:
        throw new BusinessServiceException(messageSource.getMessage("error_2006", null, Locale.getDefault()));
      case 2007:
        throw new BusinessServiceException(messageSource.getMessage("error_2007", null, Locale.getDefault()));
      case 2009:
        throw new BusinessServiceException(messageSource.getMessage("error_2009", null, Locale.getDefault()));
      case 2060:
        throw new BusinessServiceException(messageSource.getMessage("error_2060", null, Locale.getDefault()));
      case 3000:
        throw new BusinessServiceException(messageSource.getMessage("error_3000", null, Locale.getDefault()));
      case 3001:
        throw new BusinessServiceException(messageSource.getMessage("error_3001", null, Locale.getDefault()));
      case 3003:
        throw new BusinessServiceException(messageSource.getMessage("error_3003", null, Locale.getDefault()));
      case 3004:
        throw new BusinessServiceException(messageSource.getMessage("error_3004", null, Locale.getDefault()));
      case 3005:
        throw new BusinessServiceException(messageSource.getMessage("error_3005", null, Locale.getDefault()));
      case 3006:
        throw new BusinessServiceException(messageSource.getMessage("error_3006", null, Locale.getDefault()));
      case 3007:
        throw new BusinessServiceException(messageSource.getMessage("error_3007", null, Locale.getDefault()));
      case 3008:
        throw new BusinessServiceException(messageSource.getMessage("error_3008", null, Locale.getDefault()));
      case 3009:
        throw new BusinessServiceException(messageSource.getMessage("error_3009", null, Locale.getDefault()));
      case 3010:
        throw new BusinessServiceException(messageSource.getMessage("error_3010", null, Locale.getDefault()));
      case 3011:
        throw new BusinessServiceException(messageSource.getMessage("error_3011", null, Locale.getDefault()));
      case 3012:
        throw new BusinessServiceException(messageSource.getMessage("error_3012", null, Locale.getDefault()));
      case 3013:
        throw new BusinessServiceException(messageSource.getMessage("error_3013", null, Locale.getDefault()));
      case 3014:
        throw new BusinessServiceException(messageSource.getMessage("error_3014", null, Locale.getDefault()));
      case 3015:
        throw new BusinessServiceException(messageSource.getMessage("error_3015", null, Locale.getDefault()));
      case 3016:
        throw new BusinessServiceException(messageSource.getMessage("error_3016", null, Locale.getDefault()));
      case 3017:
        throw new BusinessServiceException(messageSource.getMessage("error_3017", null, Locale.getDefault()));
      case 3018:
        throw new BusinessServiceException(messageSource.getMessage("error_3018", null, Locale.getDefault()));
      case 3019:
        throw new BusinessServiceException(messageSource.getMessage("error_3019", null, Locale.getDefault()));
      case 3020:
        throw new BusinessServiceException(messageSource.getMessage("error_3020", null, Locale.getDefault()));
      case 3021:
        throw new BusinessServiceException(messageSource.getMessage("error_3021", null, Locale.getDefault()));
      case 3022:
        throw new BusinessServiceException(messageSource.getMessage("error_3022", null, Locale.getDefault()));
      case 3023:
        throw new BusinessServiceException(messageSource.getMessage("error_3023", null, Locale.getDefault()));
      case 3024:
        throw new BusinessServiceException(messageSource.getMessage("error_3024", null, Locale.getDefault()));
      case 3025:
        throw new BusinessServiceException(messageSource.getMessage("error_3025", null, Locale.getDefault()));
      case 3026:
        throw new BusinessServiceException(messageSource.getMessage("error_3026", null, Locale.getDefault()));
      case 3027:
        throw new BusinessServiceException(messageSource.getMessage("error_3027", null, Locale.getDefault()));
      case 3028:
        throw new BusinessServiceException(messageSource.getMessage("error_3028", null, Locale.getDefault()));
      case 3029:
        throw new BusinessServiceException(messageSource.getMessage("error_3029", null, Locale.getDefault()));
      case 3030:
        throw new BusinessServiceException(messageSource.getMessage("error_3030", null, Locale.getDefault()));
      case 4000:
        throw new BusinessServiceException(messageSource.getMessage("error_4000", null, Locale.getDefault()));
      case 4001:
        throw new BusinessServiceException(messageSource.getMessage("error_4001", null, Locale.getDefault()));
      case 4002:
        throw new BusinessServiceException(messageSource.getMessage("error_4002", null, Locale.getDefault()));
      case 4003:
        throw new BusinessServiceException(messageSource.getMessage("error_4003", null, Locale.getDefault()));
      case 4004:
        throw new BusinessServiceException(messageSource.getMessage("error_4004", null, Locale.getDefault()));
      case 4005:
        throw new BusinessServiceException(messageSource.getMessage("error_4005", null, Locale.getDefault()));
      case 4006:
        throw new BusinessServiceException(messageSource.getMessage("error_4006", null, Locale.getDefault()));
      case 4007:
        throw new BusinessServiceException(messageSource.getMessage("error_4007", null, Locale.getDefault()));
      case 4012:
        throw new BusinessServiceException(messageSource.getMessage("error_4012", null, Locale.getDefault()));
      case 4013:
        throw new BusinessServiceException(messageSource.getMessage("error_4013", null, Locale.getDefault()));
      case 4015:
        throw new BusinessServiceException(messageSource.getMessage("error_4015", null, Locale.getDefault()));
      case 4016:
        throw new BusinessServiceException(messageSource.getMessage("error_4016", null, Locale.getDefault()));
      case 4017:
        throw new BusinessServiceException(messageSource.getMessage("error_4017", null, Locale.getDefault()));
      case 4018:
        throw new BusinessServiceException(messageSource.getMessage("error_4018", null, Locale.getDefault()));
      case 4019:
        throw new BusinessServiceException(messageSource.getMessage("error_4019", null, Locale.getDefault()));
      case 4020:
        throw new BusinessServiceException(messageSource.getMessage("error_4020", null, Locale.getDefault()));
      case 4021:
        throw new BusinessServiceException(messageSource.getMessage("error_4021", null, Locale.getDefault()));
      case 4022:
        throw new BusinessServiceException(messageSource.getMessage("error_4022", null, Locale.getDefault()));
      case 4023:
        throw new BusinessServiceException(messageSource.getMessage("error_4023", null, Locale.getDefault()));
      case 4024:
        throw new BusinessServiceException(messageSource.getMessage("error_4024", null, Locale.getDefault()));
      case 4025:
        throw new BusinessServiceException(messageSource.getMessage("error_4025", null, Locale.getDefault()));
      case 4026:
        throw new BusinessServiceException(messageSource.getMessage("error_4026", null, Locale.getDefault()));
      case 4027:
        throw new BusinessServiceException(messageSource.getMessage("error_4027", null, Locale.getDefault()));
      case 4028:
        throw new BusinessServiceException(messageSource.getMessage("error_4028", null, Locale.getDefault()));
      case 4029:
        throw new BusinessServiceException(messageSource.getMessage("error_4029", null, Locale.getDefault()));
      case 4037:
        throw new BusinessServiceException(messageSource.getMessage("error_4037", null, Locale.getDefault()));
      case 4038:
        throw new BusinessServiceException(messageSource.getMessage("error_4038", null, Locale.getDefault()));
      case 4039:
        throw new BusinessServiceException(messageSource.getMessage("error_4039", null, Locale.getDefault()));
      case 4050:
        throw new BusinessServiceException(messageSource.getMessage("error_4050", null, Locale.getDefault()));
      case 4051:
        throw new BusinessServiceException(messageSource.getMessage("error_4051", null, Locale.getDefault()));
      default:
        throw new BusinessServiceException(messageSource.getMessage("error_1", null, Locale.getDefault()));
    }
  }
}
