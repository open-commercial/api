package sic.service.impl;

import afip.wsfe.wsdl.*;
import org.springframework.context.MessageSource;
import org.xml.sax.SAXException;
import sic.modelo.*;
import sic.modelo.embeddable.ClienteEmbeddable;
import sic.service.*;
import afip.wsaa.wsdl.LoginCms;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Locale;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ws.client.WebServiceClientException;
import sic.exception.BusinessServiceException;
import sic.exception.ServiceException;

@Service
public class AfipServiceImpl implements IAfipService {

  private final AfipWebServiceSOAPClient afipWebServiceSOAPClient;
  private final IConfiguracionSucursalService configuracionSucursalService;
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final MessageSource messageSource;
  private static final String WEBSERVICE_FACTURA_ELECTRONICA = "wsfe";
  private static final BigDecimal LIMITE_MONTO_CONSUMIDOR_FINAL = new BigDecimal(21500);
  private static final String MENSAJE_AUTORIZACION_ERROR = "mensaje_autorizacion_error";
  private static final String MENSAJE_COMPROBANTEAFIP_INVALIDO = "mensaje_comprobanteAFIP_invalido";

  @Autowired
  public AfipServiceImpl(
      AfipWebServiceSOAPClient afipWebServiceSOAPClient,
      IConfiguracionSucursalService configuracionSucursalService,
      MessageSource messageSource) {
    this.afipWebServiceSOAPClient = afipWebServiceSOAPClient;
    this.configuracionSucursalService = configuracionSucursalService;
    this.messageSource = messageSource;
  }

  @Override
  public FEAuthRequest getFEAuth(String afipNombreServicio, Sucursal sucursal) {
    FEAuthRequest feAuthRequest = new FEAuthRequest();
    ConfiguracionSucursal configuracionSucursal = sucursal.getConfiguracionSucursal();
    LocalDateTime fechaVencimientoToken = configuracionSucursal.getFechaVencimientoTokenWSAA();
    if (fechaVencimientoToken != null && fechaVencimientoToken.isAfter(LocalDateTime.now())) {
      feAuthRequest.setToken(configuracionSucursal.getTokenWSAA());
      feAuthRequest.setSign(configuracionSucursal.getSignTokenWSAA());
      feAuthRequest.setCuit(sucursal.getIdFiscal());
      return feAuthRequest;
    } else {
      byte[] p12file = configuracionSucursal.getCertificadoAfip();
      if (p12file.length == 0) {
        throw new BusinessServiceException(messageSource.getMessage(
                "mensaje_sucursal_certificado_vacio", null, Locale.getDefault()));
      } else {
        String p12signer = configuracionSucursal.getFirmanteCertificadoAfip();
        String p12pass = configuracionSucursal.getPasswordCertificadoAfip();
        long ticketTimeInHours = 12L; // siempre devuelve por 12hs
        byte[] loginTicketRequestXmlCms =
            afipWebServiceSOAPClient.crearCMS(
                p12file, p12pass, p12signer, afipNombreServicio, ticketTimeInHours);
        LoginCms loginCms = new LoginCms();
        loginCms.setIn0(Base64.getEncoder().encodeToString(loginTicketRequestXmlCms));
        try {
          String loginTicketResponse = afipWebServiceSOAPClient.loginCMS(loginCms);
          Reader tokenReader = new StringReader(loginTicketResponse);
          var saxReader = new SAXReader(false);
          saxReader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
          Document tokenDoc = saxReader.read(tokenReader);
          String tokenWSAA = tokenDoc.valueOf("/loginTicketResponse/credentials/token");
          String signTokenWSAA = tokenDoc.valueOf("/loginTicketResponse/credentials/sign");
          feAuthRequest.setToken(tokenWSAA);
          feAuthRequest.setSign(signTokenWSAA);
          feAuthRequest.setCuit(sucursal.getIdFiscal());
          configuracionSucursal.setTokenWSAA(tokenWSAA);
          configuracionSucursal.setSignTokenWSAA(signTokenWSAA);
          String generationTime = tokenDoc.valueOf("/loginTicketResponse/header/generationTime");
          String expirationTime = tokenDoc.valueOf("/loginTicketResponse/header/expirationTime");
          configuracionSucursal.setFechaGeneracionTokenWSAA(
              LocalDateTime.parse(generationTime, DateTimeFormatter.ISO_OFFSET_DATE_TIME));
          configuracionSucursal.setFechaVencimientoTokenWSAA(
              LocalDateTime.parse(expirationTime, DateTimeFormatter.ISO_OFFSET_DATE_TIME));
          configuracionSucursalService.actualizar(configuracionSucursal);
          return feAuthRequest;
        } catch (DocumentException | IOException | WebServiceClientException | SAXException ex) {
          throw new ServiceException(
                  messageSource.getMessage(MENSAJE_AUTORIZACION_ERROR, null, Locale.getDefault()), ex);
        }
      }
    }
  }

  @Override
  public void autorizar(ComprobanteAFIP comprobante) {
    if (comprobante.getTipoComprobante() != TipoDeComprobante.FACTURA_A
        && comprobante.getTipoComprobante() != TipoDeComprobante.FACTURA_B
        && comprobante.getTipoComprobante() != TipoDeComprobante.FACTURA_C
        && comprobante.getTipoComprobante() != TipoDeComprobante.NOTA_CREDITO_A
        && comprobante.getTipoComprobante() != TipoDeComprobante.NOTA_DEBITO_A
        && comprobante.getTipoComprobante() != TipoDeComprobante.NOTA_CREDITO_B
        && comprobante.getTipoComprobante() != TipoDeComprobante.NOTA_DEBITO_B
        && comprobante.getTipoComprobante() != TipoDeComprobante.NOTA_CREDITO_C
        && comprobante.getTipoComprobante() != TipoDeComprobante.NOTA_DEBITO_C) {
      throw new BusinessServiceException(messageSource.getMessage(
              MENSAJE_COMPROBANTEAFIP_INVALIDO,
              null, Locale.getDefault()));
    }
    boolean sinCae = comprobante.getCae() != 0;
    if (sinCae) {
      throw new BusinessServiceException(messageSource.getMessage(
              "mensaje_comprobanteAFIP_autorizado", null, Locale.getDefault()));
    }
    FECAESolicitar fecaeSolicitud = new FECAESolicitar();
    FEAuthRequest feAuthRequest =
        this.getFEAuth(WEBSERVICE_FACTURA_ELECTRONICA, comprobante.getSucursal());
    if (feAuthRequest != null) {
      fecaeSolicitud.setAuth(feAuthRequest);
      int nroPuntoDeVentaAfip =
          comprobante.getSucursal().getConfiguracionSucursal().getNroPuntoDeVentaAfip();
      int siguienteNroComprobante =
          this.getSiguienteNroComprobante(
              feAuthRequest, comprobante.getTipoComprobante(), nroPuntoDeVentaAfip);
      fecaeSolicitud.setFeCAEReq(
          this.transformComprobanteToFECAERequest(
              comprobante, siguienteNroComprobante, nroPuntoDeVentaAfip));
      try {
        FECAEResponse response = afipWebServiceSOAPClient.solicitarCAE(fecaeSolicitud);
        String msjError = "";
        // errores generales de la request
        if (response.getErrors() != null) {
          msjError =
              response.getErrors().getErr().get(0).getCode()
                  + "-"
                  + response.getErrors().getErr().get(0).getMsg();
          logger.error(msjError);
          throw new BusinessServiceException(msjError);
        }
        // errores particulares de cada comprobante
        if (response.getFeDetResp().getFECAEDetResponse().get(0).getResultado().equals("R")) {
          msjError +=
              response
                  .getFeDetResp()
                  .getFECAEDetResponse()
                  .get(0)
                  .getObservaciones()
                  .getObs()
                  .get(0)
                  .getMsg();
          logger.error(msjError);
          throw new BusinessServiceException(msjError);
        }
        long cae = Long.parseLong(response.getFeDetResp().getFECAEDetResponse().get(0).getCAE());
        comprobante.setCae(cae);
        String fechaVencimientoCaeResponse =
            response.getFeDetResp().getFECAEDetResponse().get(0).getCAEFchVto();
        comprobante.setVencimientoCAE(
            LocalDate.parse(fechaVencimientoCaeResponse, DateTimeFormatter.BASIC_ISO_DATE));
        comprobante.setNumSerieAfip(nroPuntoDeVentaAfip);
        comprobante.setNumFacturaAfip(siguienteNroComprobante);
      } catch (WebServiceClientException | IOException ex) {
        throw new BusinessServiceException(messageSource.getMessage(
                MENSAJE_AUTORIZACION_ERROR, null, Locale.getDefault()), ex);
      }
    }
  }

  @Override
  public int getSiguienteNroComprobante(FEAuthRequest feAuthRequest, TipoDeComprobante tipo, int nroPuntoDeVentaAfip) {
    FECompUltimoAutorizado solicitud = new FECompUltimoAutorizado();
    solicitud.setAuth(feAuthRequest);
    switch (tipo) {
      case FACTURA_A -> solicitud.setCbteTipo(1);
      case NOTA_DEBITO_A -> solicitud.setCbteTipo(2);
      case NOTA_CREDITO_A -> solicitud.setCbteTipo(3);
      case FACTURA_B -> solicitud.setCbteTipo(6);
      case NOTA_DEBITO_B -> solicitud.setCbteTipo(7);
      case NOTA_CREDITO_B -> solicitud.setCbteTipo(8);
      case FACTURA_C -> solicitud.setCbteTipo(11);
      case NOTA_DEBITO_C -> solicitud.setCbteTipo(12);
      case NOTA_CREDITO_C -> solicitud.setCbteTipo(13);
      default -> throw new BusinessServiceException(messageSource.getMessage(
              MENSAJE_COMPROBANTEAFIP_INVALIDO, null, Locale.getDefault()));
    }
    solicitud.setPtoVta(nroPuntoDeVentaAfip);
    try {
      FERecuperaLastCbteResponse response =
          afipWebServiceSOAPClient.getUltimoComprobanteAutorizado(solicitud);
      return response.getCbteNro() + 1;
    } catch (WebServiceClientException | IOException ex) {
      throw new ServiceException(messageSource.getMessage(
              MENSAJE_AUTORIZACION_ERROR, null, Locale.getDefault()), ex);
    }
  }

  @Override
  public FECAERequest transformComprobanteToFECAERequest(ComprobanteAFIP comprobante, int siguienteNroComprobante, int nroPuntoDeVentaAfip) {
    FECAERequest fecaeRequest = new FECAERequest();
    FECAECabRequest cabecera = new FECAECabRequest();
    FECAEDetRequest detalle = new FECAEDetRequest();
    this.agregarPeriodoAlDetalle(comprobante, detalle);
    // CbteTipo = 1: Factura A, 2: Nota de Débito A, 3: Nota de Crédito A, 6: Factura B,
    //    7: Nota de Débito B 8: Nota de Crédito B. 11: Factura C. 12: Nota Debito C. 13: Nota Credito C.
    // DocTipo = 80: CUIT, 86: CUIL, 96: DNI, 99: Doc.(Otro)
    int docTipo = (comprobante.getCliente().getCategoriaIVACliente() == CategoriaIVA.CONSUMIDOR_FINAL) ? 96 : 80;
    switch (comprobante.getTipoComprobante()) {
      case FACTURA_A -> {
        this.validarCliente(comprobante.getCliente());
        cabecera.setCbteTipo(1);
        detalle.setDocTipo(docTipo);
        detalle.setDocNro(comprobante.getCliente().getIdFiscalCliente());
      }
      case NOTA_DEBITO_A -> {
        this.validarCliente(comprobante.getCliente());
        cabecera.setCbteTipo(2);
        detalle.setDocTipo(docTipo);
        detalle.setDocNro(comprobante.getCliente().getIdFiscalCliente());
      }
      case NOTA_CREDITO_A -> {
        this.validarCliente(comprobante.getCliente());
        cabecera.setCbteTipo(3);
        detalle.setDocTipo(docTipo);
        detalle.setDocNro(comprobante.getCliente().getIdFiscalCliente());
      }
      case FACTURA_B -> {
        cabecera.setCbteTipo(6);
        this.procesarDetalle(detalle, comprobante);
      }
      case NOTA_DEBITO_B -> {
        cabecera.setCbteTipo(7);
        this.procesarDetalle(detalle, comprobante);
      }
      case NOTA_CREDITO_B -> {
        cabecera.setCbteTipo(8);
        this.procesarDetalle(detalle, comprobante);
      }
      case FACTURA_C -> {
        cabecera.setCbteTipo(11);
        this.procesarDetalle(detalle, comprobante);
      }
      case NOTA_DEBITO_C -> {
        cabecera.setCbteTipo(12);
        this.procesarDetalle(detalle, comprobante);
      }
      case NOTA_CREDITO_C -> {
        cabecera.setCbteTipo(13);
        this.procesarDetalle(detalle, comprobante);
      }
      default -> throw new BusinessServiceException(messageSource.getMessage(
              MENSAJE_COMPROBANTEAFIP_INVALIDO, null, Locale.getDefault()));
    }
    // Cantidad de registros del detalle del comprobante o lote de comprobantes de ingreso
    cabecera.setCantReg(1);
    // Punto de Venta del comprobante que se está informando.
    // Si se informa más de un comprobante, todos deben corresponder al mismo punto de venta
    cabecera.setPtoVta(nroPuntoDeVentaAfip);
    fecaeRequest.setFeCabReq(cabecera);
    ArrayOfFECAEDetRequest arrayDetalle = new ArrayOfFECAEDetRequest();
    detalle.setCbteDesde(siguienteNroComprobante);
    detalle.setCbteHasta(siguienteNroComprobante);
    // Concepto del Comprobante. Valores permitidos: 1 Productos, 2 Servicios, 3 Productos y
    // Servicios
    detalle.setConcepto(1);
    detalle.setCbteFch(comprobante.getFecha().format(DateTimeFormatter.BASIC_ISO_DATE));
    ArrayOfAlicIva arrayIVA = new ArrayOfAlicIva();
    if (comprobante.getIva21neto().compareTo(BigDecimal.ZERO) != 0
        && (comprobante.getTipoComprobante() != TipoDeComprobante.FACTURA_C
            && comprobante.getTipoComprobante() != TipoDeComprobante.NOTA_CREDITO_C
            && comprobante.getTipoComprobante() != TipoDeComprobante.NOTA_DEBITO_C)) {
      AlicIva alicIVA21 = new AlicIva();
      // Valores: 5 (21%), 4 (10.5%)
      alicIVA21.setId(5);
      // Se calcula con: (100 * IVA_neto) / %IVA
      alicIVA21.setBaseImp(
          comprobante
              .getIva21neto()
              .multiply(new BigDecimal("100"))
              .divide(new BigDecimal("21"), 2, RoundingMode.HALF_UP)
              .doubleValue());
      alicIVA21.setImporte(
          comprobante.getIva21neto().setScale(2, RoundingMode.HALF_UP).doubleValue());
      arrayIVA.getAlicIva().add(alicIVA21);
    }
    if (comprobante.getIva105neto().compareTo(BigDecimal.ZERO) != 0
        && (comprobante.getTipoComprobante() != TipoDeComprobante.FACTURA_C
            && comprobante.getTipoComprobante() != TipoDeComprobante.NOTA_CREDITO_C
            && comprobante.getTipoComprobante() != TipoDeComprobante.NOTA_DEBITO_C)) {
      AlicIva alicIVA105 = new AlicIva();
      // Valores: 5 (21%), 4 (10.5%)
      alicIVA105.setId(4);
      // Se calcula con: (100 * IVA_neto) / %IVA
      alicIVA105.setBaseImp(
          comprobante
              .getIva105neto()
              .multiply(new BigDecimal("100"))
              .divide(new BigDecimal("10.5"), 2, RoundingMode.HALF_UP)
              .doubleValue());
      alicIVA105.setImporte(
          comprobante.getIva105neto().setScale(2, RoundingMode.HALF_UP).doubleValue());
      arrayIVA.getAlicIva().add(alicIVA105);
    }
    // Array para informar las alícuotas y sus importes asociados a un comprobante
    // <AlicIva>. Para comprobantes tipo C y Bienes Usados – Emisor Monotributista no
    // debe informar el array.
    if (comprobante.getTipoComprobante() == TipoDeComprobante.FACTURA_C
        || comprobante.getTipoComprobante() == TipoDeComprobante.NOTA_CREDITO_C
        || comprobante.getTipoComprobante() == TipoDeComprobante.NOTA_DEBITO_C) {
      detalle.setIva(null);
    } else {
      detalle.setIva(arrayIVA);
    }
    // Suma de los importes del array de IVA. Para comprobantes tipo C debe
    // ser igual a cero (0).
    detalle.setImpIVA(
        comprobante
            .getIva105neto()
            .add(comprobante.getIva21neto())
            .setScale(2, RoundingMode.HALF_UP)
            .doubleValue());
    if (detalle.getImpIVA() == 0) {
      detalle.setIva(null);
    }
    // Importe neto gravado. Debe ser menor o igual a Importe total y no
    // puede ser menor a cero. Para comprobantes tipo C este campo
    // corresponde al Importe del Sub Total
    detalle.setImpNeto(
        comprobante
            .getSubtotalBruto()
            .setScale(2, RoundingMode.HALF_UP)
            .doubleValue());
    // El campo “Importe neto no gravado” <ImpTotConc>. No puede ser menor
    // a cero(0). Para comprobantes tipo C debe ser igual a cero (0).
    if (comprobante.getTipoComprobante() == TipoDeComprobante.FACTURA_C
        || comprobante.getTipoComprobante() == TipoDeComprobante.NOTA_CREDITO_C
        || comprobante.getTipoComprobante() == TipoDeComprobante.NOTA_DEBITO_C) {
      detalle.setImpTotConc(0);
    } else {
      detalle.setImpTotConc(
          comprobante.getMontoNoGravado().setScale(2, RoundingMode.HALF_UP).doubleValue());
    }
    // Importe total del comprobante, Debe ser igual a Importe neto no
    // gravado + Importe exento + Importe neto gravado + todos los campos
    // de IVA al XX% + Importe de tributos
    detalle.setImpTotal(
        comprobante
            .getTotal()
            .setScale(2, RoundingMode.HALF_UP)
            .doubleValue());
    // Código de moneda del comprobante. Consultar método FEParamGetTiposMonedas para valores posibles
    detalle.setMonId("PES");
    // Cotización de la moneda informada. Para PES, pesos argentinos la misma debe ser 1
    detalle.setMonCotiz(1);
    arrayDetalle.getFECAEDetRequest().add(detalle);
    fecaeRequest.setFeDetReq(arrayDetalle);
    return fecaeRequest;
  }

  private void procesarDetalle(FECAEDetRequest detalle, ComprobanteAFIP comprobante) {
    // menor a LIMITE_MONTO_CONSUMIDOR_FINAL, si DocTipo = 99 DocNro debe ser igual a 0 (simula un consumidor final ???)
    if (comprobante.getTotal().compareTo(LIMITE_MONTO_CONSUMIDOR_FINAL) < 0) {
      detalle.setDocTipo(99);
      detalle.setDocNro(0);
    } else {
      this.validarCliente(comprobante.getCliente());
      detalle.setDocTipo(
          (comprobante.getCliente().getCategoriaIVACliente() == CategoriaIVA.CONSUMIDOR_FINAL) ? 96 : 80);
      detalle.setDocNro(comprobante.getCliente().getIdFiscalCliente());
    }
  }

  private void agregarPeriodoAlDetalle(ComprobanteAFIP comprobante, FECAEDetRequest detalle) {
    if (comprobante.getTipoComprobante() == TipoDeComprobante.NOTA_CREDITO_A
        || comprobante.getTipoComprobante() == TipoDeComprobante.NOTA_CREDITO_B
        || comprobante.getTipoComprobante() == TipoDeComprobante.NOTA_CREDITO_C
        || comprobante.getTipoComprobante() == TipoDeComprobante.NOTA_DEBITO_A
        || comprobante.getTipoComprobante() == TipoDeComprobante.NOTA_DEBITO_B
        || comprobante.getTipoComprobante() == TipoDeComprobante.NOTA_DEBITO_C) {
      Periodo periodo = new Periodo();
      periodo.setFchDesde(comprobante.getFecha().format(DateTimeFormatter.BASIC_ISO_DATE));
      periodo.setFchHasta(comprobante.getFecha().format(DateTimeFormatter.BASIC_ISO_DATE));
      detalle.setPeriodoAsoc(periodo);
    }
  }

  private void validarCliente(ClienteEmbeddable cliente) {
    if (cliente.getIdFiscalCliente() == null) {
      throw new BusinessServiceException(
          messageSource.getMessage(
              "mensaje_cliente_sin_idFiscal_error", null, Locale.getDefault()));
    }
  }
}
