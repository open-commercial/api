package sic.service.impl;

import com.mercadopago.MercadoPago;
import com.mercadopago.core.MPResourceArray;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.Payment;
import com.mercadopago.resources.datastructures.payment.Payer;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import sic.exception.BusinessServiceException;
import sic.modelo.Cliente;
import sic.modelo.Recibo;
import sic.modelo.Usuario;
import sic.modelo.dto.NuevaNotaDebitoDeReciboDTO;
import sic.modelo.dto.NuevoPagoMercadoPagoDTO;
import sic.modelo.dto.PagoMercadoPagoDTO;
import sic.service.*;

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
  private final INotaService notaService;
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final ModelMapper modelMapper;
  private final MessageSource messageSource;

  @Autowired
  public PagoMercadoPagoServiceImpl(
      IReciboService reciboService,
      IFormaDePagoService formaDePagoService,
      IClienteService clienteService,
      INotaService notaService,
      ModelMapper modelMapper,
      MessageSource messageSource) {
    this.reciboService = reciboService;
    this.formaDePagoService = formaDePagoService;
    this.clienteService = clienteService;
    this.notaService = notaService;
    this.modelMapper = modelMapper;
    this.messageSource = messageSource;
  }

  @Override
  public Recibo crearNuevoPago(NuevoPagoMercadoPagoDTO nuevoPagoMercadoPagoDTO, Usuario usuario) {
    Cliente cliente =
        clienteService.getClienteNoEliminadoPorId(nuevoPagoMercadoPagoDTO.getIdCliente());
    this.validarOperacion(nuevoPagoMercadoPagoDTO, cliente);
    MercadoPago.SDK.configure(mercadoPagoAccesToken);
    Payment payment = new Payment();
    payment.setDescription(
            "("+ cliente.getNroCliente()+")"
            + " " + cliente.getNombreFiscal()
            + (cliente.getNombreFantasia() != null ? cliente.getNombreFantasia() : ""));
    Payer payer = new Payer();
    payer.setEmail(cliente.getEmail());
    payment.setPayer(payer);
    payment.setBinaryMode(true);
    if (nuevoPagoMercadoPagoDTO.getToken() != null
        && !nuevoPagoMercadoPagoDTO.getToken().isEmpty()) {
      payment
          .setTransactionAmount(nuevoPagoMercadoPagoDTO.getMonto())
          .setToken(nuevoPagoMercadoPagoDTO.getToken())
          .setInstallments(nuevoPagoMercadoPagoDTO.getInstallments())
          .setIssuerId(nuevoPagoMercadoPagoDTO.getIssuerId())
          .setPaymentMethodId(nuevoPagoMercadoPagoDTO.getPaymentMethodId());
    } else {
      payment
          .setTransactionAmount(nuevoPagoMercadoPagoDTO.getMonto())
          .setPaymentMethodId(nuevoPagoMercadoPagoDTO.getPaymentMethodId());
    }
    Recibo nuevoRecibo = new Recibo();
    try {
      payment = payment.save();
      if (payment.getStatus() == Payment.Status.approved) {
        logger.warn("El pago de mercadopago {} se aprobó correctamente.", payment);
        nuevoRecibo.setEmpresa(cliente.getEmpresa());
        nuevoRecibo.setFormaDePago(formaDePagoService.getFormasDePagoPorId(16));
        nuevoRecibo.setUsuario(usuario);
        nuevoRecibo.setCliente(cliente);
        nuevoRecibo.setFecha(new Date());
        nuevoRecibo.setConcepto("Pago en Mercadopago");
        nuevoRecibo.setMonto(new BigDecimal(Float.toString(nuevoPagoMercadoPagoDTO.getMonto())));
        nuevoRecibo.setIdPagoMercadoPago(payment.getId());
        nuevoRecibo = reciboService.guardar(nuevoRecibo);
      } else {
        logger.warn("El pago {} no fue aprobado", payment);
        this.procesarMensajeNoAprobado(payment);
      }

    } catch (MPException exception) {
      logger.error(exception.toString());
      throw new BusinessServiceException(
          messageSource.getMessage(
              exception.getStatusCode().toString(), null, Locale.getDefault()));
    }
    return nuevoRecibo;
  }

  private void procesarMensajeNoAprobado(Payment payment) {
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
  }

  private void validarOperacion(NuevoPagoMercadoPagoDTO nuevoPagoMercadoPagoDTO, Cliente cliente) {
    if (cliente.getEmail() == null || cliente.getEmail().isEmpty()) {
      throw new BusinessServiceException(
          messageSource.getMessage("mensaje_pago_cliente_sin_email", null, Locale.getDefault()));
    }
    if (nuevoPagoMercadoPagoDTO.getPaymentMethodId() != null
        && nuevoPagoMercadoPagoDTO.getPaymentMethodId().isEmpty()) {
      throw new BusinessServiceException(
          messageSource.getMessage(
              "mensaje_pago_sin_payment_method_id", null, Locale.getDefault()));
    }
    if (nuevoPagoMercadoPagoDTO.getToken() != null
        && !nuevoPagoMercadoPagoDTO.getToken().isEmpty()
        && (nuevoPagoMercadoPagoDTO.getIssuerId() == null
            || nuevoPagoMercadoPagoDTO.getIssuerId().isEmpty())) {
      throw new BusinessServiceException(
          messageSource.getMessage("mensaje_pago_sin_issuer_id", null, Locale.getDefault()));
    }
    if (nuevoPagoMercadoPagoDTO.getInstallments() != null) {
      nuevoPagoMercadoPagoDTO.setInstallments(1);
    }
  }

  @Override
  public PagoMercadoPagoDTO recuperarPago(String idPago) {
    MercadoPago.SDK.configure(mercadoPagoAccesToken);
    PagoMercadoPagoDTO pagoRecuperado;
    try {
      Payment pagoMP = Payment.findById(idPago);
      pagoRecuperado = modelMapper.map(pagoMP, PagoMercadoPagoDTO.class);
    } catch (MPException e) {
      throw new BusinessServiceException(
          messageSource.getMessage(e.getStatusCode().toString(), null, Locale.getDefault()));
    }
    return pagoRecuperado;
  }

  @Override
  public NuevoPagoMercadoPagoDTO devolverPago(String idPago, Usuario usuario) {
    MercadoPago.SDK.configure(mercadoPagoAccesToken);
    NuevoPagoMercadoPagoDTO pagoRecuperado = new NuevoPagoMercadoPagoDTO();
    try {
      Payment pagoMP = Payment.findById(idPago);
      pagoMP = pagoMP.refund();
      Recibo reciboDeMercadoPago = reciboService.getReciboPorIdMercadoPago(idPago);
      pagoRecuperado.setInstallments(pagoMP.getInstallments());
      pagoRecuperado.setIdCliente(reciboDeMercadoPago.getIdCliente());
      pagoRecuperado.setIssuerId(pagoMP.getIssuerId());
      pagoRecuperado.setMonto(reciboDeMercadoPago.getMonto().floatValue());
      pagoRecuperado.setPaymentMethodId(pagoMP.getPaymentMethodId());
      NuevaNotaDebitoDeReciboDTO nuevaNotaDebitoDeReciboDTO =
          NuevaNotaDebitoDeReciboDTO.builder()
              .idRecibo(reciboDeMercadoPago.getIdRecibo())
              .gastoAdministrativo(BigDecimal.ZERO)
              .motivo("Devolución de pago por MercadoPago")
              .tipoDeComprobante(
                  notaService
                      .getTipoNotaDebitoCliente(
                          reciboDeMercadoPago.getIdCliente(),
                          reciboDeMercadoPago.getEmpresa().getId_Empresa())
                      .get(0))
              .build();
      notaService.guardarNotaDebito(
          notaService.calcularNotaDebitoConRecibo(nuevaNotaDebitoDeReciboDTO, usuario));
    } catch (MPException | NullPointerException e) {
      logger.error(e.toString());
    }
    return pagoRecuperado;
  }

  @Override
  public PagoMercadoPagoDTO[] recuperarPagosPendientesDeClientePorMail(String email) {
    MercadoPago.SDK.configure(mercadoPagoAccesToken);
    HashMap<String, String> filtros = new HashMap<>();
    filtros.put("payer.email", email);
    filtros.put("status", "in_mediation");
    MPResourceArray resultados = null;
    try {
      resultados = Payment.search(filtros, false);
    } catch (MPException e) {
      logger.error(e.toString());
    }
    return (resultados != null)
        ? modelMapper.map(resultados.resources(), PagoMercadoPagoDTO[].class)
        : null;
  }
}
