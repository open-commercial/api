package sic.service.impl;

import com.mercadopago.MercadoPago;
import com.mercadopago.core.MPApiResponse;
import com.mercadopago.exceptions.MPRestException;
import com.mercadopago.resources.Payment;
import com.mercadopago.resources.datastructures.payment.Payer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import sic.modelo.Cliente;
import sic.modelo.Recibo;
import sic.modelo.dto.PagoMercadoPagoDTO;
import sic.service.IFormaDePagoService;
import sic.service.IPagoMercadoPagoService;
import sic.service.IReciboService;

import java.math.BigDecimal;
import java.util.Date;

@Service
@Validated
public class PagoMercadoPagoServiceImpl implements IPagoMercadoPagoService {

  @Value("${SIC_MERCADOPAGO_ACCESS_TOKEN}")
  private String mercadoPagoAccesToken;

  private final IReciboService reciboService;
  private final IFormaDePagoService formaDePagoService;

  @Autowired
  public PagoMercadoPagoServiceImpl(IReciboService reciboService,
                                    IFormaDePagoService formaDePagoService) {
    this.reciboService = reciboService;
    this.formaDePagoService = formaDePagoService;
  }

  @Override
  public boolean crearNuevoPago(PagoMercadoPagoDTO pagoMercadoPagoDTO, Cliente cliente, Float monto) {
    MercadoPago.SDK.configure(mercadoPagoAccesToken);
//    try {
//      MPApiResponse paymentMethods = MercadoPago.SDK.Get("/v1/payment_methods"); ///v1/payment_methods
//    } catch (MPRestException e) {
//      e.printStackTrace();
//    }
    Payment payment = new Payment();
    if (pagoMercadoPagoDTO.getToken() != null && !pagoMercadoPagoDTO.getToken().isEmpty()){
    payment
        .setTransactionAmount(monto)
        .setToken(pagoMercadoPagoDTO.getToken())
        .setDescription("Pago Mercado Pago - TESTING con token")
        .setInstallments(pagoMercadoPagoDTO.getInstallments())
        .setIssuerId(pagoMercadoPagoDTO.getIssuerId())
        .setPaymentMethodId(pagoMercadoPagoDTO.getPaymentMethodId())
        .setBinaryMode(true)
        .setPayer(new Payer().setEmail(cliente.getEmail()));
    } else {
      payment.setTransactionAmount(monto)
        .setDescription("Pago Mercado Pago - TESTING sin token")
        .setPaymentMethodId(pagoMercadoPagoDTO.getPaymentMethodId())
        .setPayer(new Payer().setEmail(cliente.getEmail()));
    }
    boolean operacionExitosa = false;
    try {
      payment = payment.save();
      if (payment.getStatus() == Payment.Status.approved) {
        Recibo nuevoRecibo = new Recibo();
        nuevoRecibo.setEmpresa(cliente.getEmpresa());
        nuevoRecibo.setFormaDePago(formaDePagoService.getFormasDePagoPorId(16));
        nuevoRecibo.setUsuario(cliente.getCredencial());
        nuevoRecibo.setCliente(cliente);
        nuevoRecibo.setFecha(new Date());
        nuevoRecibo.setConcepto("probando pago");
        nuevoRecibo.setMonto(new BigDecimal(Float.toString(monto)));
        operacionExitosa = true;
      }
    } catch (Exception e) {
    }
    return operacionExitosa;
  }
}
