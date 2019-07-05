package sic.service.impl;

import com.mercadopago.MercadoPago;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.Payment;
import com.mercadopago.resources.datastructures.payment.Payer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import sic.modelo.Cliente;
import sic.modelo.Recibo;
import sic.modelo.dto.PagoMercadoPagoDTO;
import sic.service.*;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Date;
import java.util.ResourceBundle;

@Service
@Validated
public class PagoMercadoPagoServiceImpl implements IPagoMercadoPagoService {

  @Value("${SIC_MERCADOPAGO_ACCESS_TOKEN}")
  private String mercadoPagoAccesToken;

  private final IReciboService reciboService;
  private final IFormaDePagoService formaDePagoService;
  private final IClienteService clienteService;
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("Mensajes");

  @Autowired
  public PagoMercadoPagoServiceImpl(
      IReciboService reciboService,
      IFormaDePagoService formaDePagoService,
      IClienteService clienteService) {
    this.reciboService = reciboService;
    this.formaDePagoService = formaDePagoService;
    this.clienteService = clienteService;
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
          RESOURCE_BUNDLE.getString("mensaje_pago_sin_payment_method_id"));
    }
    if (pagoMercadoPagoDTO.getToken() != null && !pagoMercadoPagoDTO.getToken().isEmpty()) {
      if (pagoMercadoPagoDTO.getIssuerId() != null && pagoMercadoPagoDTO.getIssuerId().isEmpty()) {
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("mensaje_pago_sin_issuer_id"));
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
      throw new BusinessServiceException(RESOURCE_BUNDLE.getString("mensaje_pago_no_soportado"));
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
      logger.error("El pago no se pudo recuperar.");
    }
    return pagoRecuperado;
  }

  private void lanzarExcepcionSegunDetalleDePayment(Payment payment) {
    switch (payment.getStatusDetail()) {
      case "pending_contingency":
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("pending_contingency"));
      case "pending_review_manual":
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("pending_review_manual"));
      case "cc_rejected_bad_filled_card_number":
        throw new BusinessServiceException(
            RESOURCE_BUNDLE.getString("cc_rejected_bad_filled_card_number"));
      case "cc_rejected_bad_filled_date":
        throw new BusinessServiceException(
            RESOURCE_BUNDLE.getString("cc_rejected_bad_filled_date"));
      case "cc_rejected_bad_filled_other":
        throw new BusinessServiceException(
            RESOURCE_BUNDLE.getString("cc_rejected_bad_filled_other"));
      case "cc_rejected_bad_filled_security_code":
        throw new BusinessServiceException(
            RESOURCE_BUNDLE.getString("cc_rejected_bad_filled_security_code"));
      case "cc_rejected_blacklist":
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("cc_rejected_blacklist"));
      case "cc_rejected_call_for_authorize":
        throw new BusinessServiceException(
            RESOURCE_BUNDLE.getString("cc_rejected_call_for_authorize"));
      case "cc_rejected_card_disabled":
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("cc_rejected_card_disabled"));
      case "cc_rejected_card_error":
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("cc_rejected_card_error"));
      case "cc_rejected_duplicated_payment":
        throw new BusinessServiceException(
            RESOURCE_BUNDLE.getString("cc_rejected_duplicated_payment"));
      case "cc_rejected_high_risk":
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("cc_rejected_high_risk"));
      case "cc_rejected_insufficient_amount":
        throw new BusinessServiceException(
            RESOURCE_BUNDLE.getString("cc_rejected_insufficient_amount"));
      case "cc_rejected_invalid_installments":
        throw new BusinessServiceException(
            MessageFormat.format(
                RESOURCE_BUNDLE.getString("cc_rejected_invalid_installments"),
                payment.getInstallments()));
      case "cc_rejected_max_attempts":
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("cc_rejected_max_attempts"));
      default:
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("cc_rejected_other_reason"));
    }
  }

  private void lanzarBussinesExceptionPorExceptionMercadoPago(MPException exception) {
    switch (exception.getStatusCode()) {
      case 1:
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("error_1"));
      case 3:
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("error_3"));
      case 5:
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("error_5"));
      case 1000:
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("error_1000"));
      case 1001:
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("error_1001"));
      case 2001:
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("error_2001"));
      case 2004:
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("error_2004"));
      case 2002:
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("error_2002"));
      case 2006:
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("error_2006"));
      case 2007:
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("error_2007"));
      case 2009:
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("error_2009"));
      case 2060:
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("error_2060"));
      case 3000:
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("error_3000"));
      case 3001:
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("error_3001"));
      case 3003:
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("error_3003"));
      case 3004:
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("error_3004"));
      case 3005:
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("error_3005"));
      case 3006:
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("error_3006"));
      case 3007:
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("error_3007"));
      case 3008:
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("error_3008"));
      case 3009:
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("error_3009"));
      case 3010:
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("error_3010"));
      case 3011:
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("error_3011"));
      case 3012:
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("error_3012"));
      case 3013:
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("error_3013"));
      case 3014:
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("error_3014"));
      case 3015:
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("error_3015"));
      case 3016:
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("error_3016"));
      case 3017:
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("error_3017"));
      case 3018:
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("error_3018"));
      case 3019:
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("error_3019"));
      case 3020:
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("error_3020"));
      case 3021:
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("error_3021"));
      case 3022:
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("error_3022"));
      case 3023:
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("error_3023"));
      case 3024:
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("error_3024"));
      case 3025:
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("error_3025"));
      case 3026:
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("error_3026"));
      case 3027:
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("error_3027"));
      case 3028:
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("error_3028"));
      case 3029:
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("error_3029"));
      case 3030:
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("error_3030"));
      case 4000:
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("error_4000"));
      case 4001:
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("error_4001"));
      case 4002:
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("error_4002"));
      case 4003:
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("error_4003"));
      case 4004:
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("error_4004"));
      case 4005:
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("error_4005"));
      case 4006:
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("error_4006"));
      case 4007:
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("error_4007"));
      case 4012:
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("error_4012"));
      case 4013:
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("error_4013"));
      case 4015:
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("error_4015"));
      case 4016:
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("error_4016"));
      case 4017:
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("error_4017"));
      case 4018:
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("error_4018"));
      case 4019:
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("error_4019"));
      case 4020:
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("error_4020"));
      case 4021:
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("error_4021"));
      case 4022:
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("error_4022"));
      case 4023:
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("error_4023"));
      case 4024:
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("error_4024"));
      case 4025:
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("error_4025"));
      case 4026:
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("error_4026"));
      case 4027:
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("error_4027"));
      case 4028:
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("error_4028"));
      case 4029:
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("error_4029"));
      case 4037:
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("error_4037"));
      case 4038:
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("error_4038"));
      case 4039:
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("error_4039"));
      case 4050:
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("error_4050"));
      case 4051:
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("error_4051"));
      default:
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("error_1"));
    }
  }
}
