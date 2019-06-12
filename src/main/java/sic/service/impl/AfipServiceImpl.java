package sic.service.impl;

import sic.modelo.*;
import sic.service.*;
import afip.wsaa.wsdl.LoginCms;
import afip.wsfe.wsdl.AlicIva;
import afip.wsfe.wsdl.ArrayOfAlicIva;
import afip.wsfe.wsdl.ArrayOfFECAEDetRequest;
import afip.wsfe.wsdl.FEAuthRequest;
import afip.wsfe.wsdl.FECAECabRequest;
import afip.wsfe.wsdl.FECAEDetRequest;
import afip.wsfe.wsdl.FECAERequest;
import afip.wsfe.wsdl.FECAEResponse;
import afip.wsfe.wsdl.FECAESolicitar;
import afip.wsfe.wsdl.FECompUltimoAutorizado;
import afip.wsfe.wsdl.FERecuperaLastCbteResponse;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.ResourceBundle;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ws.client.WebServiceClientException;
import sic.util.FormatterFechaHora;

@Service
public class AfipServiceImpl implements IAfipService {

  private final AfipWebServiceSOAPClient afipWebServiceSOAPClient;
  private final IConfiguracionDelSistemaService configuracionDelSistemaService;
  private final IFacturaService facturaService;
  private final INotaService notaService;
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private static final String WEBSERVICE_FACTURA_ELECTRONICA = "wsfe";
  private static final BigDecimal LIMITE_MONTO_CONSUMIDOR_FINAL = new BigDecimal(5000);
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("Mensajes");

  @Autowired
  public AfipServiceImpl(
      AfipWebServiceSOAPClient afipWebServiceSOAPClient, IConfiguracionDelSistemaService cds,
      IFacturaService facturaService, INotaService notaService) {
    this.afipWebServiceSOAPClient = afipWebServiceSOAPClient;
    this.configuracionDelSistemaService = cds;
    this.facturaService = facturaService;
    this.notaService = notaService;
  }

  @Override
  public FEAuthRequest getFEAuth(String afipNombreServicio, Empresa empresa) {
    FEAuthRequest feAuthRequest = new FEAuthRequest();
    ConfiguracionDelSistema cds =
        configuracionDelSistemaService.getConfiguracionDelSistemaPorEmpresa(empresa);
    Date fechaVencimientoToken = cds.getFechaVencimientoTokenWSAA();
    if (fechaVencimientoToken != null && fechaVencimientoToken.after(new Date())) {
      feAuthRequest.setToken(cds.getTokenWSAA());
      feAuthRequest.setSign(cds.getSignTokenWSAA());
      feAuthRequest.setCuit(empresa.getIdFiscal());
      return feAuthRequest;
    } else {
      byte[] p12file = cds.getCertificadoAfip();
      if (p12file.length == 0) {
        throw new BusinessServiceException(
            RESOURCE_BUNDLE.getString("mensaje_cds_certificado_vacio"));
      }
      String p12signer = cds.getFirmanteCertificadoAfip();
      String p12pass = cds.getPasswordCertificadoAfip();
      long ticketTime = 3600000L; // siempre devuelve por 12hs
      byte[] loginTicketRequestXmlCms =
          afipWebServiceSOAPClient.crearCMS(
              p12file, p12pass, p12signer, afipNombreServicio, ticketTime);
      LoginCms loginCms = new LoginCms();
      loginCms.setIn0(Base64.getEncoder().encodeToString(loginTicketRequestXmlCms));
      try {
        String loginTicketResponse = afipWebServiceSOAPClient.loginCMS(loginCms);
        Reader tokenReader = new StringReader(loginTicketResponse);
        Document tokenDoc = new SAXReader(false).read(tokenReader);
        String tokenWSAA = tokenDoc.valueOf("/loginTicketResponse/credentials/token");
        String signTokenWSAA = tokenDoc.valueOf("/loginTicketResponse/credentials/sign");
        feAuthRequest.setToken(tokenWSAA);
        feAuthRequest.setSign(signTokenWSAA);
        feAuthRequest.setCuit(empresa.getIdFiscal());
        cds.setTokenWSAA(tokenWSAA);
        cds.setSignTokenWSAA(signTokenWSAA);
        String generationTime = tokenDoc.valueOf("/loginTicketResponse/header/generationTime");
        String expirationTime = tokenDoc.valueOf("/loginTicketResponse/header/expirationTime");
        SimpleDateFormat sdf =
            new SimpleDateFormat(FormatterFechaHora.FORMATO_FECHAHORA_INTERNACIONAL_MILISEGUNDO);
        cds.setFechaGeneracionTokenWSAA(sdf.parse(generationTime));
        cds.setFechaVencimientoTokenWSAA(sdf.parse(expirationTime));
        configuracionDelSistemaService.actualizar(cds);
        return feAuthRequest;
      } catch (DocumentException | IOException ex) {
        logger.error(ex.getMessage());
        throw new ServiceException(RESOURCE_BUNDLE.getString("mensaje_error_procesando_xml"), ex);
      } catch (ParseException ex) {
        logger.error(ex.getMessage());
        throw new ServiceException(RESOURCE_BUNDLE.getString("mensaje_error_procesando_fecha"), ex);
      } catch (WebServiceClientException ex) {
        logger.error(ex.getMessage());
        throw new ServiceException(RESOURCE_BUNDLE.getString("mensaje_token_wsaa_error"), ex);
      }
    }
  }

  @Override
  public void autorizar(ComprobanteAFIP comprobante) {
    if (!configuracionDelSistemaService
        .getConfiguracionDelSistemaPorEmpresa(comprobante.getEmpresa())
        .isFacturaElectronicaHabilitada()) {
      throw new BusinessServiceException(RESOURCE_BUNDLE.getString("mensaje_cds_fe_habilitada"));
    }
    if (comprobante.getTipoComprobante() != TipoDeComprobante.FACTURA_A
        && comprobante.getTipoComprobante() != TipoDeComprobante.FACTURA_B
        && comprobante.getTipoComprobante() != TipoDeComprobante.FACTURA_C
        && comprobante.getTipoComprobante() != TipoDeComprobante.NOTA_CREDITO_A
        && comprobante.getTipoComprobante() != TipoDeComprobante.NOTA_DEBITO_A
        && comprobante.getTipoComprobante() != TipoDeComprobante.NOTA_CREDITO_B
        && comprobante.getTipoComprobante() != TipoDeComprobante.NOTA_DEBITO_B
        && comprobante.getTipoComprobante() != TipoDeComprobante.NOTA_CREDITO_C
        && comprobante.getTipoComprobante() != TipoDeComprobante.NOTA_DEBITO_C) {
      throw new BusinessServiceException(
          RESOURCE_BUNDLE.getString("mensaje_comprobanteAFIP_invalido"));
    } else {
      if ((comprobante.getTipoComprobante() == TipoDeComprobante.FACTURA_A
              || comprobante.getTipoComprobante() == TipoDeComprobante.FACTURA_B
              || comprobante.getTipoComprobante() == TipoDeComprobante.FACTURA_C)
          && facturaService.existeFacturaVentaAnteriorSinAutorizar(comprobante)) {
        throw new BusinessServiceException(
            ResourceBundle.getBundle("Mensajes")
                .getString("mensaje_existe_comprobante_anterior_sin_autorizar"));
      }
      if ((comprobante.getTipoComprobante() == TipoDeComprobante.NOTA_CREDITO_A
              || comprobante.getTipoComprobante() == TipoDeComprobante.NOTA_CREDITO_B
              || comprobante.getTipoComprobante() == TipoDeComprobante.NOTA_CREDITO_C)
          && notaService.existeNotaCreditoAnteriorSinAutorizar(comprobante)) {
        throw new BusinessServiceException(
            ResourceBundle.getBundle("Mensajes")
                .getString("mensaje_existe_comprobante_anterior_sin_autorizar"));
      }
      if ((comprobante.getTipoComprobante() == TipoDeComprobante.NOTA_DEBITO_A
        || comprobante.getTipoComprobante() == TipoDeComprobante.NOTA_DEBITO_B
        || comprobante.getTipoComprobante() == TipoDeComprobante.NOTA_DEBITO_C)
        && notaService.existeNotaDebitoAnteriorSinAutorizar(comprobante)) {
        throw new BusinessServiceException(
          ResourceBundle.getBundle("Mensajes")
            .getString("mensaje_existe_comprobante_anterior_sin_autorizar"));
      }
    }
    if (comprobante.getCAE() != 0) {
      throw new BusinessServiceException(
          RESOURCE_BUNDLE.getString("mensaje_comprobanteAFIP_autorizado"));
    }
    FECAESolicitar fecaeSolicitud = new FECAESolicitar();
    FEAuthRequest feAuthRequest =
        this.getFEAuth(WEBSERVICE_FACTURA_ELECTRONICA, comprobante.getEmpresa());
    fecaeSolicitud.setAuth(feAuthRequest);
    int nroPuntoDeVentaAfip =
        configuracionDelSistemaService
            .getConfiguracionDelSistemaPorEmpresa(comprobante.getEmpresa())
            .getNroPuntoDeVentaAfip();
    int siguienteNroComprobante =
        this.getSiguienteNroComprobante(
            feAuthRequest, comprobante.getTipoComprobante(), nroPuntoDeVentaAfip);
    fecaeSolicitud.setFeCAEReq(
        this.transformComprobanteToFECAERequest(
            comprobante, siguienteNroComprobante, nroPuntoDeVentaAfip));
    try {
      FECAEResponse response = afipWebServiceSOAPClient.FECAESolicitar(fecaeSolicitud);
      String msjError = "";
      // errores generales de la request
      if (response.getErrors() != null) {
        msjError =
            response.getErrors().getErr().get(0).getCode()
                + "-"
                + response.getErrors().getErr().get(0).getMsg();
        logger.error(msjError);
        if (!msjError.isEmpty()) {
          throw new BusinessServiceException(msjError);
        }
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
      SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
      comprobante.setCAE(cae);
      comprobante.setVencimientoCAE(
          formatter.parse(response.getFeDetResp().getFECAEDetResponse().get(0).getCAEFchVto()));
      comprobante.setNumSerieAfip(nroPuntoDeVentaAfip);
      comprobante.setNumFacturaAfip(siguienteNroComprobante);
    } catch (WebServiceClientException ex) {
      logger.error(ex.getMessage());
      throw new ServiceException(RESOURCE_BUNDLE.getString("mensaje_autorizacion_error"), ex);
    } catch (ParseException ex) {
      logger.error(ex.getMessage());
      throw new ServiceException(RESOURCE_BUNDLE.getString("mensaje_error_procesando_fecha"), ex);
    } catch (IOException ex) {
      logger.error(ex.getMessage());
      throw new ServiceException(RESOURCE_BUNDLE.getString("mensaje_error_procesando_xml"), ex);
    }
  }

  @Override
  public int getSiguienteNroComprobante(
      FEAuthRequest feAuthRequest, TipoDeComprobante tipo, int nroPuntoDeVentaAfip) {
    FECompUltimoAutorizado solicitud = new FECompUltimoAutorizado();
    solicitud.setAuth(feAuthRequest);
    switch (tipo) {
      case FACTURA_A:
        solicitud.setCbteTipo(1);
        break;
      case NOTA_DEBITO_A:
        solicitud.setCbteTipo(2);
        break;
      case NOTA_CREDITO_A:
        solicitud.setCbteTipo(3);
        break;
      case FACTURA_B:
        solicitud.setCbteTipo(6);
        break;
      case NOTA_DEBITO_B:
        solicitud.setCbteTipo(7);
        break;
      case NOTA_CREDITO_B:
        solicitud.setCbteTipo(8);
        break;
      case FACTURA_C:
        solicitud.setCbteTipo(11);
        break;
      case NOTA_DEBITO_C:
        solicitud.setCbteTipo(12);
        break;
      case NOTA_CREDITO_C:
        solicitud.setCbteTipo(13);
        break;
      default:
        throw new BusinessServiceException(
            RESOURCE_BUNDLE.getString("mensaje_comprobanteAFIP_invalido"));
    }
    solicitud.setPtoVta(nroPuntoDeVentaAfip);
    try {
      FERecuperaLastCbteResponse response =
          afipWebServiceSOAPClient.FECompUltimoAutorizado(solicitud);
      return response.getCbteNro() + 1;
    } catch (WebServiceClientException ex) {
      logger.error(ex.getMessage());
      throw new ServiceException(
          RESOURCE_BUNDLE.getString("mensaje_siguiente_nro_comprobante_error"), ex);
    } catch (IOException ex) {
      logger.error(ex.getMessage());
      throw new ServiceException(RESOURCE_BUNDLE.getString("mensaje_error_procesando_xml"), ex);
    }
  }

  @Override
  public FECAERequest transformComprobanteToFECAERequest(
      ComprobanteAFIP comprobante, int siguienteNroComprobante, int nroPuntoDeVentaAfip) {
    FECAERequest fecaeRequest = new FECAERequest();
    FECAECabRequest cabecera = new FECAECabRequest();
    FECAEDetRequest detalle = new FECAEDetRequest();
    // CbteTipo = 1: Factura A, 2: Nota de Débito A, 3: Nota de Crédito A, 6: Factura B,
    //    7: Nota de Débito B 8: Nota de Crédito B. 11: Factura C. 12: Nota Debito C. 13: Nota Credito C.
    // DocTipo = 80: CUIT, 86: CUIL, 96: DNI, 99: Doc.(Otro)
    int docTipo = (comprobante.getCliente().getCategoriaIVA() == CategoriaIVA.CONSUMIDOR_FINAL) ? 96 : 80;
    switch (comprobante.getTipoComprobante()) {
      case FACTURA_A:
        cabecera.setCbteTipo(1);
        detalle.setDocTipo(docTipo);
        detalle.setDocNro(comprobante.getCliente().getIdFiscal());
        break;
      case NOTA_DEBITO_A:
        cabecera.setCbteTipo(2);
        detalle.setDocTipo(docTipo);
        detalle.setDocNro(comprobante.getCliente().getIdFiscal());
        break;
      case NOTA_CREDITO_A:
        cabecera.setCbteTipo(3);
        detalle.setDocTipo(docTipo);
        detalle.setDocNro(comprobante.getCliente().getIdFiscal());
        break;
      case FACTURA_B:
        cabecera.setCbteTipo(6);
        // menor a $5000, si DocTipo = 99 DocNro debe ser igual a 0 (simula un consumidor final ???)
        if (comprobante.getTotal().compareTo(LIMITE_MONTO_CONSUMIDOR_FINAL) < 0) {
          detalle.setDocTipo(99);
          detalle.setDocNro(0);
        } else {
          if (comprobante.getCliente().getIdFiscal() == null) {
            throw new BusinessServiceException(
                RESOURCE_BUNDLE.getString("mensaje_cliente_sin_idFiscal_error"));
          }
          detalle.setDocTipo(docTipo);
          detalle.setDocNro(comprobante.getCliente().getIdFiscal());
        }
        break;
      case NOTA_DEBITO_B:
        cabecera.setCbteTipo(7);
        // menor a $5000, si DocTipo = 99 DocNro debe ser igual a 0 (simula un consumidor final ???)
        if (comprobante.getTotal().compareTo(LIMITE_MONTO_CONSUMIDOR_FINAL) < 0) {
          detalle.setDocTipo(99);
          detalle.setDocNro(0);
        } else {
          if (comprobante.getCliente().getIdFiscal() == null) {
            throw new BusinessServiceException(
                RESOURCE_BUNDLE.getString("mensaje_cliente_sin_idFiscal_error"));
          }
          detalle.setDocTipo(docTipo);
          detalle.setDocNro(comprobante.getCliente().getIdFiscal());
        }
        break;
      case NOTA_CREDITO_B:
        cabecera.setCbteTipo(8);
        // menor a $5000, si DocTipo = 99 DocNro debe ser igual a 0 (simula un consumidor final ???)
        if (comprobante.getTotal().compareTo(LIMITE_MONTO_CONSUMIDOR_FINAL) < 0) {
          detalle.setDocTipo(99);
          detalle.setDocNro(0);
        } else {
          if (comprobante.getCliente().getIdFiscal() == null) {
            throw new BusinessServiceException(
                RESOURCE_BUNDLE.getString("mensaje_cliente_sin_idFiscal_error"));
          }
          detalle.setDocTipo(docTipo);
          detalle.setDocNro(comprobante.getCliente().getIdFiscal());
        }
        break;
      case FACTURA_C:
        cabecera.setCbteTipo(11);
        if (comprobante.getTotal().compareTo(LIMITE_MONTO_CONSUMIDOR_FINAL) < 0) {
          detalle.setDocTipo(99);
          detalle.setDocNro(0);
        } else {
          if (comprobante.getCliente().getIdFiscal() == null) {
            throw new BusinessServiceException(
              RESOURCE_BUNDLE.getString("mensaje_cliente_sin_idFiscal_error"));
          }
          detalle.setDocTipo(docTipo);
          detalle.setDocNro(comprobante.getCliente().getIdFiscal());
        }
        break;
      case NOTA_DEBITO_C:
        cabecera.setCbteTipo(12);
        if (comprobante.getTotal().compareTo(LIMITE_MONTO_CONSUMIDOR_FINAL) < 0) {
          detalle.setDocTipo(99);
          detalle.setDocNro(0);
        } else {
          if (comprobante.getCliente().getIdFiscal() == null) {
            throw new BusinessServiceException(
              RESOURCE_BUNDLE.getString("mensaje_cliente_sin_idFiscal_error"));
          }
          detalle.setDocTipo(docTipo);
          detalle.setDocNro(comprobante.getCliente().getIdFiscal());
        }
        break;
      case NOTA_CREDITO_C:
        cabecera.setCbteTipo(13);
        if (comprobante.getTotal().compareTo(LIMITE_MONTO_CONSUMIDOR_FINAL) < 0) {
          detalle.setDocTipo(99);
          detalle.setDocNro(0);
        } else {
          if (comprobante.getCliente().getIdFiscal() == null) {
            throw new BusinessServiceException(
              RESOURCE_BUNDLE.getString("mensaje_cliente_sin_idFiscal_error"));
          }
          detalle.setDocTipo(docTipo);
          detalle.setDocNro(comprobante.getCliente().getIdFiscal());
        }
        break;
      default:
        throw new BusinessServiceException(
            RESOURCE_BUNDLE.getString("mensaje_comprobanteAFIP_invalido"));
    }
    // Cantidad de registros del detalle del comprobante o lote de comprobantes de ingreso
    cabecera.setCantReg(1);
    // Punto de Venta del comprobante que se está informando.
    // Si se informa más de un comprobante, todos deben corresponder al mismo punto de venta
    cabecera.setPtoVta(nroPuntoDeVentaAfip);
    fecaeRequest.setFeCabReq(cabecera);
    SimpleDateFormat sdf = new SimpleDateFormat(FormatterFechaHora.FORMATO_FECHA_INTERNACIONAL);
    ArrayOfFECAEDetRequest arrayDetalle = new ArrayOfFECAEDetRequest();
    detalle.setCbteDesde(siguienteNroComprobante);
    detalle.setCbteHasta(siguienteNroComprobante);
    // Concepto del Comprobante. Valores permitidos: 1 Productos, 2 Servicios, 3 Productos y Servicios
    detalle.setConcepto(1);
    // Fecha del comprobante (yyyymmdd)
    detalle.setCbteFch(
        sdf.format(comprobante.getFecha()).replace("/", ""));
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
}
