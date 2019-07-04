package sic.service.impl;

import com.google.gson.JsonElement;
import com.mercadopago.MercadoPago;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.exceptions.MPRestException;
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
  public PagoMercadoPagoServiceImpl(IReciboService reciboService,
                                    IFormaDePagoService formaDePagoService,
                                    IClienteService clienteService) {
    this.reciboService = reciboService;
    this.formaDePagoService = formaDePagoService;
    this.clienteService = clienteService;
  }

  @Override
  public boolean crearNuevoPago(PagoMercadoPagoDTO pagoMercadoPagoDTO) {
    Cliente cliente = clienteService.getClienteNoEliminadoPorId(pagoMercadoPagoDTO.getIdCliente());
    MercadoPago.SDK.configure(mercadoPagoAccesToken);
    Payment payment = new Payment();
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
          .setDescription("Pago Mercado Pago - TESTING con token")
          .setInstallments(pagoMercadoPagoDTO.getInstallments())
          .setIssuerId(pagoMercadoPagoDTO.getIssuerId())
          .setPaymentMethodId(pagoMercadoPagoDTO.getPaymentMethodId())
          .setBinaryMode(true)
          .setPayer(new Payer().setEmail(cliente.getEmail()));
    } else if (pagoMercadoPagoDTO.getToken() == null || pagoMercadoPagoDTO.getToken().isEmpty()) {
      payment
          .setTransactionAmount(pagoMercadoPagoDTO.getMonto())
          .setDescription("Pago Mercado Pago - TESTING sin token")
          .setPaymentMethodId(pagoMercadoPagoDTO.getPaymentMethodId())
          .setPayer(new Payer().setEmail(cliente.getEmail()));
    } else {
      throw new BusinessServiceException(RESOURCE_BUNDLE.getString("mensaje_pago_no_soportado"));
    }
    boolean operacionExitosa = false;
    try {
      payment = payment.save();
      if (payment.getStatus() == Payment.Status.approved) {
        logger.warn("El pago de mercadopago {} se aprobó correctamente.", payment);
        Recibo nuevoRecibo = new Recibo();
        nuevoRecibo.setEmpresa(cliente.getEmpresa());
        nuevoRecibo.setFormaDePago(formaDePagoService.getFormasDePagoPorId(16));
        nuevoRecibo.setUsuario(cliente.getCredencial());
        nuevoRecibo.setCliente(cliente);
        nuevoRecibo.setFecha(new Date());
        nuevoRecibo.setConcepto("probando pago");
        nuevoRecibo.setMonto(new BigDecimal(Float.toString(pagoMercadoPagoDTO.getMonto())));
        nuevoRecibo.setIdPagoMercadoPago(new Long(payment.getId()));
        reciboService.guardar(nuevoRecibo);
        operacionExitosa = true;
      } else {
        logger.warn("El pago {} no se aprobó.", payment);
      }
    } catch (MPException e) {
      logger.error(e.toString());
    }
    return operacionExitosa;
  }

  @Override
  public Payment recuperarPago(String idPago) {
    MercadoPago.SDK.configure(mercadoPagoAccesToken);
    Payment pagoRecuperado = null;
    try {
      pagoRecuperado = Payment.findById(idPago);
    } catch (MPException e) {
      e.printStackTrace();
    }
    return pagoRecuperado;
  }
}
