package org.opencommercial.service;

import afip.wsaa.wsdl.LoginCms;
import afip.wsaa.wsdl.LoginCmsResponse;
import afip.wsfe.wsdl.*;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.cms.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.opencommercial.exception.BusinessServiceException;
import org.opencommercial.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.xml.transform.StringResult;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;

@Slf4j
public class AfipWebServiceSOAPClient extends WebServiceGatewaySupport {

  private static final String SOAP_ACTION_FE_CAE_SOLICITAR = "http://ar.gov.afip.dif.FEV1/FECAESolicitar";
  private static final String SOAP_ACTION_FE_COMPROBANTE_ULTIMO_AUTORIZADO = "http://ar.gov.afip.dif.FEV1/FECompUltimoAutorizado";
  private static final String MENSAJE_SERVICIO_NO_CONFIGURADO = "mensaje_taxation_afip_no_configurado";

  @Autowired
  private MessageSource messageSource;

  @Value("${AFIP_WS_AUTH_URI}")
  private String afipWsAuthUri;

  @Value("${AFIP_WS_FE_URI}")
  private String afipWsFeUri;

  public boolean isServicioConfigurado() {
    return afipWsAuthUri != null && !afipWsAuthUri.isEmpty()
            && afipWsFeUri != null && !afipWsFeUri.isEmpty();
  }

  public String loginCMS(LoginCms loginCMS) throws IOException {
    if (!isServicioConfigurado())
      throw new ServiceException(messageSource.getMessage(MENSAJE_SERVICIO_NO_CONFIGURADO, null, Locale.getDefault()));
    var result = new StringResult();
    this.getWebServiceTemplate().getMarshaller().marshal(loginCMS, result);
    log.info("TOKEN WSAA XML REQUEST: {}", result);
    var response = (LoginCmsResponse) this.getWebServiceTemplate().marshalSendAndReceive(afipWsAuthUri, loginCMS);
    this.getWebServiceTemplate().getMarshaller().marshal(response, result);
    log.info("TOKEN WSAA XML RESPONSE: {}", result);
    return response.getLoginCmsReturn();
  }

  public byte[] crearCMS(byte[] p12file, String p12pass, String signer, String service, long ticketTimeInHours) {
    if (!isServicioConfigurado())
      throw new ServiceException(messageSource.getMessage(MENSAJE_SERVICIO_NO_CONFIGURADO, null, Locale.getDefault()));
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
      certStore = CertStore.getInstance("Collection", new CollectionCertStoreParameters(certList), "BC");
    } catch (KeyStoreException
        | IOException
        | NoSuchAlgorithmException
        | CertificateException
        | UnrecoverableKeyException
        | InvalidAlgorithmParameterException
        | NoSuchProviderException ex) {
      throw new BusinessServiceException(
          messageSource.getMessage("mensaje_certificado_error", null, Locale.getDefault()), ex);
    }
    String loginTicketRequestXml = this.crearTicketRequerimientoAcceso(service, ticketTimeInHours);
    try {
      CMSSignedDataGenerator generator = new CMSSignedDataGenerator();
      generator.addSigner(pKey, pCertificate, CMSSignedGenerator.DIGEST_SHA1);
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
      throw new BusinessServiceException(
              messageSource.getMessage("mensaje_firmando_certificado_error", null, Locale.getDefault()), ex);
    }
    return asn1Cms;
  }

  public String crearTicketRequerimientoAcceso(String service, long ticketTimeInHours) {
    if (!isServicioConfigurado())
      throw new ServiceException(messageSource.getMessage(MENSAJE_SERVICIO_NO_CONFIGURADO, null, Locale.getDefault()));
    var now = LocalDateTime.now();
    var zdt = now.atZone(ZoneId.systemDefault());
    var uniqueId = Long.toString(zdt.toInstant().toEpochMilli() / 1000);
    var xmlgentime = LocalDateTime.now()
            .atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    var xmlexptime = LocalDateTime.now()
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

  public FERecuperaLastCbteResponse getUltimoComprobanteAutorizado(FECompUltimoAutorizado solicitud) throws IOException {
    if (!isServicioConfigurado())
      throw new ServiceException(messageSource.getMessage(MENSAJE_SERVICIO_NO_CONFIGURADO, null, Locale.getDefault()));
    var result = new StringResult();
    this.getWebServiceTemplate().getMarshaller().marshal(solicitud, result);
    log.info("ULTIMO COMPROBANTE AUTORIZADO XML REQUEST: {}", result);
    var response =
            (FECompUltimoAutorizadoResponse)
                    this.getWebServiceTemplate()
                            .marshalSendAndReceive(
                                    afipWsFeUri,
                                    solicitud,
                                    (WebServiceMessage message) ->
                                            ((SoapMessage) message).setSoapAction(SOAP_ACTION_FE_COMPROBANTE_ULTIMO_AUTORIZADO));
    this.getWebServiceTemplate().getMarshaller().marshal(response, result);
    log.info("ULTIMO COMPROBANTE AUTORIZADO XML RESPONSE: {}", result);
    return response.getFECompUltimoAutorizadoResult();
  }

  public FECAEResponse solicitarCAE(FECAESolicitar solicitud) throws IOException {
    if (!isServicioConfigurado())
      throw new ServiceException(messageSource.getMessage(MENSAJE_SERVICIO_NO_CONFIGURADO, null, Locale.getDefault()));
    var result = new StringResult();
    this.getWebServiceTemplate().getMarshaller().marshal(solicitud, result);
    log.info("SOLICITAR cae XML REQUEST: {}", result);
    var response =
            (FECAESolicitarResponse)
                    this.getWebServiceTemplate()
                            .marshalSendAndReceive(
                                    afipWsFeUri,
                                    solicitud,
                                    (WebServiceMessage message) ->
                                            ((SoapMessage) message).setSoapAction(SOAP_ACTION_FE_CAE_SOLICITAR));
    this.getWebServiceTemplate().getMarshaller().marshal(response, result);
    log.info("SOLICITAR cae XML RESPONSE: {}", result);
    return response.getFECAESolicitarResult();
  }
}
