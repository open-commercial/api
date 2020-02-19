package sic.service.impl;

import afip.wsaa.wsdl.LoginCms;
import afip.wsaa.wsdl.LoginCmsResponse;
import afip.wsfe.wsdl.FECAEResponse;
import afip.wsfe.wsdl.FECAESolicitar;
import afip.wsfe.wsdl.FECAESolicitarResponse;
import afip.wsfe.wsdl.FECompUltimoAutorizado;
import afip.wsfe.wsdl.FECompUltimoAutorizadoResponse;
import afip.wsfe.wsdl.FERecuperaLastCbteResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertStore;
import java.security.cert.CertStoreException;
import java.security.cert.CertificateException;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;
import javax.xml.transform.Result;

import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessable;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.xml.transform.StringResult;
import sic.exception.BusinessServiceException;
import sic.exception.ServiceException;

public class AfipWebServiceSOAPClient extends WebServiceGatewaySupport {

  private static final String WSAA_TESTING = "https://wsaahomo.afip.gov.ar/ws/services/LoginCms";
  private static final String WSAA_PRODUCTION = "https://wsaa.afip.gov.ar/ws/services/LoginCms";
  private static final String WSFE_TESTING = "https://wswhomo.afip.gov.ar/wsfev1/service.asmx";
  private static final String WSFE_PRODUCTION =
      "https://servicios1.afip.gov.ar/wsfev1/service.asmx";
  private final Logger loggerSoapClient = LoggerFactory.getLogger(this.getClass());
  private static final String SOAP_ACTION_FECAESolicitar =
      "http://ar.gov.afip.dif.FEV1/FECAESolicitar";
  private static final String SOAP_ACTION_FECompUltimoAutorizado =
      "http://ar.gov.afip.dif.FEV1/FECompUltimoAutorizado";

  @Autowired private MessageSource messageSource;

  @Value("${SIC_AFIP_ENV}")
  private String afipEnvironment;

  public String getWSAA_URI() {
    switch (afipEnvironment) {
      case "production":
        return WSAA_PRODUCTION;
      case "testing":
        return WSAA_TESTING;
      default:
        throw new ServiceException(
            messageSource.getMessage("mensaje_error_env_no_soportado", null, Locale.getDefault()));
    }
  }

  public String getWSFE_URI() {
    switch (afipEnvironment) {
      case "production":
        return WSFE_PRODUCTION;
      case "testing":
        return WSFE_TESTING;
      default:
        throw new ServiceException(
            messageSource.getMessage("mensaje_error_env_no_soportado", null, Locale.getDefault()));
    }
  }

  public String loginCMS(LoginCms loginCMS) throws IOException {
    Result result = new StringResult();
    this.getWebServiceTemplate().getMarshaller().marshal(loginCMS, result);
    loggerSoapClient.warn("TOKEN WSAA XML REQUEST: {}", result);
    LoginCmsResponse response =
        (LoginCmsResponse)
            this.getWebServiceTemplate().marshalSendAndReceive(this.getWSAA_URI(), loginCMS);
    this.getWebServiceTemplate().getMarshaller().marshal(response, result);
    loggerSoapClient.warn("TOKEN WSAA XML RESPONSE: {}", result);
    return response.getLoginCmsReturn();
  }

  public byte[] crearCMS(
      byte[] p12file, String p12pass, String signer, String service, long ticketTimeInHours) {
    PrivateKey pKey;
    X509Certificate pCertificate;
    byte[] asn1Cms;
    CertStore certStore;
    try {
      KeyStore ks = KeyStore.getInstance("pkcs12");
      InputStream is = new ByteArrayInputStream(p12file);
      ks.load(is, p12pass.toCharArray());
      is.close();
      pKey = (PrivateKey) ks.getKey(signer, p12pass.toCharArray());
      pCertificate = (X509Certificate) ks.getCertificate(signer);
      ArrayList<X509Certificate> certList = new ArrayList<>();
      certList.add(pCertificate);
      if (Security.getProvider("BC") == null) {
        Security.addProvider(new BouncyCastleProvider());
      }
      certStore =
          CertStore.getInstance("Collection", new CollectionCertStoreParameters(certList), "BC");
    } catch (KeyStoreException
        | IOException
        | NoSuchAlgorithmException
        | CertificateException
        | UnrecoverableKeyException
        | InvalidAlgorithmParameterException
        | NoSuchProviderException ex) {
      loggerSoapClient.error(ex.getMessage());
      throw new BusinessServiceException(
          messageSource.getMessage("mensaje_certificado_error", null, Locale.getDefault()));
    }
    String loginTicketRequestXml = this.crearTicketRequerimientoAcceso(service, ticketTimeInHours);
    try {
      CMSSignedDataGenerator generator = new CMSSignedDataGenerator();
      generator.addSigner(pKey, pCertificate, CMSSignedDataGenerator.DIGEST_SHA1);
      generator.addCertificatesAndCRLs(certStore);
      CMSProcessable data = new CMSProcessableByteArray(loginTicketRequestXml.getBytes());
      CMSSignedData signed = generator.generate(data, true, "BC");
      asn1Cms = signed.getEncoded();
    } catch (IllegalArgumentException
        | CertStoreException
        | CMSException
        | NoSuchAlgorithmException
        | NoSuchProviderException
        | IOException ex) {
      loggerSoapClient.error(ex.getMessage());
      throw new BusinessServiceException(
          messageSource.getMessage(
              "mensaje_firmando_certificado_error", null, Locale.getDefault()));
    }
    return asn1Cms;
  }

  public String crearTicketRequerimientoAcceso(String service, long ticketTimeInHours) {
    // long uniqueId = 1L + (long) (Math.random() * (9999999999L - 1L));
    LocalDateTime now = LocalDateTime.now();
    ZonedDateTime zdt = now.atZone(ZoneId.systemDefault());
    String uniqueId = Long.toString(zdt.toInstant().toEpochMilli() / 1000);
    String xmlgentime =
        LocalDateTime.now()
            .atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    String xmlexptime =
        LocalDateTime.now()
            .plusHours(ticketTimeInHours)
            .atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
        + "<loginTicketRequest version=\"1.0\">"
        + "<header>"
        + "<uniqueId>" + uniqueId + "</uniqueId>"
        + "<generationTime>" + xmlgentime + "</generationTime>"
        + "<expirationTime>" + xmlexptime + "</expirationTime>"
        + "</header>"
        + "<service>" + service + "</service>"
        + "</loginTicketRequest>";
  }

  public FERecuperaLastCbteResponse FECompUltimoAutorizado(FECompUltimoAutorizado solicitud)
      throws IOException {
    Result result = new StringResult();
    this.getWebServiceTemplate().getMarshaller().marshal(solicitud, result);
    loggerSoapClient.warn("ULTIMO COMPROBANTE AUTORIZADO XML REQUEST: {}", result);
    FECompUltimoAutorizadoResponse response =
        (FECompUltimoAutorizadoResponse)
            this.getWebServiceTemplate()
                .marshalSendAndReceive(
                    this.getWSFE_URI(),
                    solicitud,
                    (WebServiceMessage message) ->
                        ((SoapMessage) message).setSoapAction(SOAP_ACTION_FECompUltimoAutorizado));
    this.getWebServiceTemplate().getMarshaller().marshal(response, result);
    loggerSoapClient.warn("ULTIMO COMPROBANTE AUTORIZADO XML RESPONSE: {}", result);
    return response.getFECompUltimoAutorizadoResult();
  }

  public FECAEResponse FECAESolicitar(FECAESolicitar solicitud) throws IOException {
    Result result = new StringResult();
    this.getWebServiceTemplate().getMarshaller().marshal(solicitud, result);
    loggerSoapClient.warn("SOLICITAR cae XML REQUEST: {}", result);
    FECAESolicitarResponse response =
        (FECAESolicitarResponse)
            this.getWebServiceTemplate()
                .marshalSendAndReceive(
                    this.getWSFE_URI(),
                    solicitud,
                    (WebServiceMessage message) ->
                        ((SoapMessage) message).setSoapAction(SOAP_ACTION_FECAESolicitar));
    this.getWebServiceTemplate().getMarshaller().marshal(response, result);
    loggerSoapClient.warn("SOLICITAR cae XML RESPONSE: {}", result);
    return response.getFECAESolicitarResult();
  }
}
