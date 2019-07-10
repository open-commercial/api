package sic.service.impl;

import com.mercadopago.MercadoPago;
import com.mercadopago.core.MPResourceArray;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.Payment;
import com.mercadopago.resources.datastructures.payment.Payer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import sic.exception.BusinessServiceException;
import sic.modelo.Cliente;
import sic.modelo.Recibo;
import sic.modelo.dto.PagoMercadoPagoDTO;
import sic.service.*;

import javax.validation.constraints.Email;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

@Service
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
    this.validarOperacion(pagoMercadoPagoDTO, cliente);
    MercadoPago.SDK.configure(mercadoPagoAccesToken);
    Payment payment = new Payment();
    payment.setDescription(
        "Pago de "
            + cliente.getNombreFiscal()
            + "("
            + cliente.getNroCliente()
            + ") con "
            + pagoMercadoPagoDTO.getPaymentMethodId());
    Payer payer = new Payer();
    payer.setEmail(cliente.getEmail());
    payment.setPayer(payer);
    payment.setBinaryMode(true);
    if (pagoMercadoPagoDTO.getToken() != null && !pagoMercadoPagoDTO.getToken().isEmpty()) {
      payment
          .setTransactionAmount(pagoMercadoPagoDTO.getMonto())
          .setToken(pagoMercadoPagoDTO.getToken())
          .setInstallments(pagoMercadoPagoDTO.getInstallments())
          .setIssuerId(pagoMercadoPagoDTO.getIssuerId())
          .setPaymentMethodId(pagoMercadoPagoDTO.getPaymentMethodId());
    } else {
      payment
          .setTransactionAmount(pagoMercadoPagoDTO.getMonto())
          .setPaymentMethodId(pagoMercadoPagoDTO.getPaymentMethodId());
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
        nuevoRecibo.setConcepto("Pago en Mercadopago");
        nuevoRecibo.setMonto(new BigDecimal(Float.toString(pagoMercadoPagoDTO.getMonto())));
        nuevoRecibo.setIdPagoMercadoPago(payment.getId());
        nuevoRecibo = reciboService.guardar(nuevoRecibo);
      } else {
        throw new BusinessServiceException(
            messageSource.getMessage(payment.getStatusDetail(), null, Locale.getDefault()));
      }
    } catch (MPException exception) {
      logger.error(exception.toString());
      throw new BusinessServiceException(
          messageSource.getMessage(
              exception.getStatusCode().toString(), null, Locale.getDefault()));
    }
    return nuevoRecibo;
  }

  private void validarOperacion(PagoMercadoPagoDTO pagoMercadoPagoDTO, Cliente cliente) {
    if (cliente.getEmail() == null || cliente.getEmail().isEmpty()) {
      throw new BusinessServiceException(
          messageSource.getMessage("mensaje_pago_cliente_sin_email", null, Locale.getDefault()));
    }
    if (pagoMercadoPagoDTO.getPaymentMethodId() != null
        && pagoMercadoPagoDTO.getPaymentMethodId().isEmpty()) {
      throw new BusinessServiceException(
          messageSource.getMessage(
              "mensaje_pago_sin_payment_method_id", null, Locale.getDefault()));
    }
    if (pagoMercadoPagoDTO.getToken() != null
        && !pagoMercadoPagoDTO.getToken().isEmpty()
        && (pagoMercadoPagoDTO.getIssuerId() == null
            || pagoMercadoPagoDTO.getIssuerId().isEmpty())) {
      throw new BusinessServiceException(
          messageSource.getMessage("mensaje_pago_sin_issuer_id", null, Locale.getDefault()));
    }
    if (pagoMercadoPagoDTO.getInstallments() != null) {
      pagoMercadoPagoDTO.setInstallments(1);
    }
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
    } catch (MPException e) {
      throw new BusinessServiceException(
        messageSource.getMessage(e.getStatusCode().toString(), null, Locale.getDefault()));
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
      //nota credito
    } catch (MPException | NullPointerException e) {
      logger.error(e.toString());
    }
    return pagoRecuperado;
  }

  @Override
  public MPResourceArray recuperarPagosPendientesDeClientePorMail(@Email String email) {
    HashMap<String, String> filtros = new HashMap<>();
    filtros.put("payer.email", email);
    MPResourceArray resultados = null;
    try {
      resultados = Payment.search(filtros, true);
    } catch (MPException e) {
      logger.error(e.toString());
    }
    return resultados;
  }

}
