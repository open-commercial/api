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
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.ResourceBundle;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessable;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.soap.SoapMessage;
import sic.service.BusinessServiceException;
import sic.util.Utilidades;

public class AfipWebServiceSOAPClient extends WebServiceGatewaySupport {
    
    private final String WSAA_TESTING = "https://wsaahomo.afip.gov.ar/ws/services/LoginCms";
    private final String WSAA_PRODUCTION = "https://wsaa.afip.gov.ar/ws/services/LoginCms";    
    private final String WSFE_TESTING = "https://wswhomo.afip.gov.ar/wsfev1/service.asmx";
    private final String WSFE_PRODUCTION = "https://servicios1.afip.gov.ar/wsfev1/service.asmx";
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    private final String SOAP_ACTION_FECAESolicitar = "http://ar.gov.afip.dif.FEV1/FECAESolicitar";
    private final String SOAP_ACTION_FECompUltimoAutorizado = "http://ar.gov.afip.dif.FEV1/FECompUltimoAutorizado";

    @Value("${SIC_AFIP_ENV}")
    private String afipEnvironment;

    public String getWSAA_URI() {
        if (afipEnvironment.equals("production")) {
            return WSAA_PRODUCTION;
        } else {
            return WSAA_TESTING;
        }
    }
    
    public String getWSFE_URI() {
        if (afipEnvironment.equals("production")) {
            return WSFE_PRODUCTION;
        } else {
            return WSFE_TESTING;
        }
    }
    
    public String loginCMS(LoginCms loginCMS) {
        LoginCmsResponse response = (LoginCmsResponse) this.getWebServiceTemplate()
                .marshalSendAndReceive(this.getWSAA_URI(), loginCMS);
        return response.getLoginCmsReturn();
    }

    public byte[] crearCMS(byte[] p12file, String p12pass, String signer, String service, long ticketTime) {
        PrivateKey pKey = null;
        X509Certificate pCertificate = null;
        byte[] asn1_cms = null;
        CertStore cstore = null;
        try {
            KeyStore ks = KeyStore.getInstance("pkcs12");
            InputStream is;
            is = Utilidades.convertirByteArrayToInputStream(p12file);
            ks.load(is, p12pass.toCharArray());
            is.close();
            pKey = (PrivateKey) ks.getKey(signer, p12pass.toCharArray());
            pCertificate = (X509Certificate) ks.getCertificate(signer);
            ArrayList<X509Certificate> certList = new ArrayList<>();
            certList.add(pCertificate);
            if (Security.getProvider("BC") == null) {
                Security.addProvider(new BouncyCastleProvider());
            }
            cstore = CertStore.getInstance("Collection", new CollectionCertStoreParameters(certList), "BC");
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException 
                | UnrecoverableKeyException | InvalidAlgorithmParameterException | NoSuchProviderException ex) {            
            LOGGER.error(ex.getMessage());
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes").getString("mensaje_certificado_error"));
        }
        String loginTicketRequest_xml = this.crearTicketRequerimientoAcceso(service, ticketTime);
        try {
            CMSSignedDataGenerator generator = new CMSSignedDataGenerator();
            generator.addSigner(pKey, pCertificate, CMSSignedDataGenerator.DIGEST_SHA1);
            generator.addCertificatesAndCRLs(cstore);
            CMSProcessable data = new CMSProcessableByteArray(loginTicketRequest_xml.getBytes());
            CMSSignedData signed = generator.generate(data, true, "BC");
            asn1_cms = signed.getEncoded();
        } catch (IllegalArgumentException | CertStoreException | CMSException | NoSuchAlgorithmException | NoSuchProviderException | IOException ex) {
            LOGGER.error(ex.getMessage());
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes").getString("mensaje_firmando_certificado_error"));
        }
        return asn1_cms;
    }

    public String crearTicketRequerimientoAcceso(String service, long ticketTime) {
        Date now = new Date();
        GregorianCalendar genenerationTime = new GregorianCalendar();
        GregorianCalendar expirationTime = new GregorianCalendar();
        DatatypeFactory datatypeFactory = null;
        String uniqueId = Long.toString(now.getTime() / 1000);
        expirationTime.setTime(new Date(now.getTime() + ticketTime));
        try {
            datatypeFactory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException ex) {
            LOGGER.error(ex.getMessage());
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes").getString("mensaje_error_xml_factory"));
        }
        XMLGregorianCalendar XMLGenTime = datatypeFactory.newXMLGregorianCalendar(genenerationTime);
        XMLGregorianCalendar XMLExpTime = datatypeFactory.newXMLGregorianCalendar(expirationTime);
        String LoginTicketRequest_xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                                        + "<loginTicketRequest version=\"1.0\">"
                                        + "<header>"
                                        + "<uniqueId>" + uniqueId + "</uniqueId>"
                                        + "<generationTime>" + XMLGenTime + "</generationTime>"
                                        + "<expirationTime>" + XMLExpTime + "</expirationTime>"
                                        + "</header>"
                                        + "<service>" + service + "</service>"
                                        + "</loginTicketRequest>";
        return LoginTicketRequest_xml;
    }

    public FERecuperaLastCbteResponse FECompUltimoAutorizado(FECompUltimoAutorizado solicitud) {
        FECompUltimoAutorizadoResponse response = (FECompUltimoAutorizadoResponse) this.getWebServiceTemplate()
                .marshalSendAndReceive(this.getWSFE_URI(), solicitud, (WebServiceMessage message) -> {
                    ((SoapMessage) message).setSoapAction(SOAP_ACTION_FECompUltimoAutorizado);
        });        
        return response.getFECompUltimoAutorizadoResult();
    }
    
    public FECAEResponse FECAESolicitar(FECAESolicitar solicitud) {
        FECAESolicitarResponse response = (FECAESolicitarResponse) this.getWebServiceTemplate()
                .marshalSendAndReceive(this.getWSFE_URI(), solicitud, (WebServiceMessage message) -> {
                    ((SoapMessage) message).setSoapAction(SOAP_ACTION_FECAESolicitar);
        });        
        return response.getFECAESolicitarResult();
    }
}
