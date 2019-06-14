package sic.service.impl;

import com.mercadopago.MercadoPago;
import com.mercadopago.resources.Payment;
import com.mercadopago.resources.datastructures.payment.Payer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import sic.modelo.Cliente;
import sic.modelo.Recibo;
import sic.modelo.dto.PagoMercadoPagoDTO;
import sic.service.IPagoMercadoPagoService;
import sic.service.IReciboService;

@Service
@Validated
public class PagoMercadoPagoServiceImpl implements IPagoMercadoPagoService {

  @Value("${SIC_MERCADOPAGO_ACCESS_TOKEN}")
  private String mercadoPagoAccesToken;

  private final IReciboService reciboService;

  @Autowired
  public PagoMercadoPagoServiceImpl(IReciboService reciboService) {
    this.reciboService = reciboService;
  }

  @Override
  public boolean crearNuevoPago(PagoMercadoPagoDTO pagoMercadoPagoDTO, Cliente cliente, Float monto) {
    MercadoPago.SDK.configure(mercadoPagoAccesToken);
    Payment payment = new Payment();
    payment
        .setTransactionAmount(monto)
        .setToken(pagoMercadoPagoDTO.getToken())
        .setDescription("Pago Mercado Pago - TESTING")
        .setInstallments(pagoMercadoPagoDTO.getInstallments())
        .setIssuerId(pagoMercadoPagoDTO.getIssuerId())
        .setPaymentMethodId(pagoMercadoPagoDTO.getPaymentMethodId())
        .setBinaryMode(true)
        .setPayer(new Payer().setEmail(cliente.getEmail()));
    try {
      payment = payment.save();
      if (payment.getStatus() == Payment.Status.approved) {
        Recibo nuevoRecibo = new Recibo();
        nuevoRecibo.setEmpresa(cliente.getEmpresa());
      //  nuevoRecibo.setFormaDePago();
      }
    } catch (Exception e) {
    }
    return (payment.getStatus() == Payment.Status.approved);
  }
}
