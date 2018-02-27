package sic.service.impl;

import sic.service.IAfipService;
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
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.ResourceBundle;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ws.client.WebServiceClientException;
import sic.modelo.ComprobanteAFIP;
import sic.modelo.Empresa;
import sic.modelo.TipoDeComprobante;
import sic.service.BusinessServiceException;
import sic.service.IConfiguracionDelSistemaService;
import sic.util.FormatterFechaHora;

@Service
@Transactional
public class AfipServiceImpl implements IAfipService {

    private final AfipWebServiceSOAPClient afipWebServiceSOAPClient;
    private final IConfiguracionDelSistemaService configuracionDelSistemaService;
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());    
    private final FormatterFechaHora formatterFechaHora = new FormatterFechaHora(FormatterFechaHora.FORMATO_FECHA_INTERNACIONAL);
    private final static String WEBSERVICE_FACTURA_ELECTRONICA = "wsfe";

    @Autowired
    public AfipServiceImpl(AfipWebServiceSOAPClient afipWebServiceSOAPClient, IConfiguracionDelSistemaService cds) {
        this.afipWebServiceSOAPClient = afipWebServiceSOAPClient;
        this.configuracionDelSistemaService = cds;
    }

    @Override
    public FEAuthRequest getFEAuth(String afipNombreServicio, Empresa empresa) {
        FEAuthRequest feAuthRequest = new FEAuthRequest();
        String loginTicketResponse = "";
        byte[] p12file = configuracionDelSistemaService.getConfiguracionDelSistemaPorEmpresa(empresa).getCertificadoAfip();
        if (p12file.length == 0) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes").getString("mensaje_cds_certificado_vacio"));
        }
        String p12signer = configuracionDelSistemaService.getConfiguracionDelSistemaPorEmpresa(empresa).getFirmanteCertificadoAfip();
        String p12pass = configuracionDelSistemaService.getConfiguracionDelSistemaPorEmpresa(empresa).getPasswordCertificadoAfip();
        long ticketTime = 3600000L; //siempre devuelve por 12hs
        byte[] loginTicketRequest_xml_cms = afipWebServiceSOAPClient.crearCMS(p12file, p12pass, p12signer, afipNombreServicio, ticketTime);
        LoginCms loginCms = new LoginCms();
        loginCms.setIn0(Base64.getEncoder().encodeToString(loginTicketRequest_xml_cms));
        try {
            loginTicketResponse = afipWebServiceSOAPClient.loginCMS(loginCms);        
        } catch (WebServiceClientException ex) {
            LOGGER.error(ex.getMessage());            
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes").getString("mensaje_token_wsaa_error"));
        }
        try {
            Reader tokenReader = new StringReader(loginTicketResponse);
            Document tokenDoc = new SAXReader(false).read(tokenReader);
            feAuthRequest.setToken(tokenDoc.valueOf("/loginTicketResponse/credentials/token"));
            feAuthRequest.setSign(tokenDoc.valueOf("/loginTicketResponse/credentials/sign"));
            feAuthRequest.setCuit(empresa.getCuip());
        } catch (DocumentException ex) {
            LOGGER.error(ex.getMessage());
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes").getString("mensaje_error_procesando_xml"));
        }
        return feAuthRequest;
    }
    
    @Override    
    public void autorizar(ComprobanteAFIP comprobante) {
        if (configuracionDelSistemaService.getConfiguracionDelSistemaPorEmpresa(comprobante.getEmpresa()).isFacturaElectronicaHabilitada() == false) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes").getString("mensaje_cds_fe_habilitada"));
        }
        if (comprobante.getTipoComprobante() != TipoDeComprobante.FACTURA_A && comprobante.getTipoComprobante() != TipoDeComprobante.FACTURA_B
                && comprobante.getTipoComprobante() != TipoDeComprobante.FACTURA_C && comprobante.getTipoComprobante() != TipoDeComprobante.NOTA_CREDITO_A
                && comprobante.getTipoComprobante() != TipoDeComprobante.NOTA_DEBITO_A && comprobante.getTipoComprobante() != TipoDeComprobante.NOTA_CREDITO_B
                && comprobante.getTipoComprobante() != TipoDeComprobante.NOTA_DEBITO_B) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes").getString("mensaje_comprobanteAFIP_invalido"));
        }
        if (comprobante.getCAE() != 0) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes").getString("mensaje_comprobanteAFIP_autorizado"));
        }
        FECAESolicitar fecaeSolicitud = new FECAESolicitar();
        FEAuthRequest feAuthRequest = this.getFEAuth(WEBSERVICE_FACTURA_ELECTRONICA, comprobante.getEmpresa());
        fecaeSolicitud.setAuth(feAuthRequest);
        int nroPuntoDeVentaAfip = configuracionDelSistemaService.getConfiguracionDelSistemaPorEmpresa(comprobante.getEmpresa()).getNroPuntoDeVentaAfip();
        int siguienteNroComprobante = this.getSiguienteNroComprobante(feAuthRequest, comprobante.getTipoComprobante(), nroPuntoDeVentaAfip);        
        fecaeSolicitud.setFeCAEReq(this.transformComprobanteToFECAERequest(comprobante, siguienteNroComprobante, nroPuntoDeVentaAfip));        
        try {
            FECAEResponse response = afipWebServiceSOAPClient.FECAESolicitar(fecaeSolicitud);
            String msjError = "";
            // errores generales de la request
            if (response.getErrors() != null) {
                msjError = response.getErrors().getErr().get(0).getCode() + "-" + response.getErrors().getErr().get(0).getMsg();
                LOGGER.error(msjError);
                if (!msjError.isEmpty()) {
                    throw new BusinessServiceException(msjError);
                }
            }
            // errores particulares de cada comprobante
            if (response.getFeDetResp().getFECAEDetResponse().get(0).getResultado().equals("R")) {
                msjError += response.getFeDetResp().getFECAEDetResponse().get(0).getObservaciones().getObs().get(0).getMsg();
                LOGGER.error(msjError);
                throw new BusinessServiceException(msjError);
            }            
            long cae = Long.valueOf(response.getFeDetResp().getFECAEDetResponse().get(0).getCAE());
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
            comprobante.setCAE(cae);
            comprobante.setVencimientoCAE(formatter.parse(response.getFeDetResp().getFECAEDetResponse().get(0).getCAEFchVto()));
            comprobante.setNumSerieAfip(nroPuntoDeVentaAfip);
            comprobante.setNumFacturaAfip(siguienteNroComprobante);
        } catch (WebServiceClientException ex) {
            LOGGER.error(ex.getMessage());
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes").getString("mensaje_autorizacion_error"));
        } catch (ParseException ex) {
            LOGGER.error(ex.getMessage());
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes").getString("mensaje_error_procesando_fecha"));
        }
    }
    
    @Override
    public int getSiguienteNroComprobante(FEAuthRequest feAuthRequest, TipoDeComprobante tipo, int nroPuntoDeVentaAfip) {
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
        }
        solicitud.setPtoVta(nroPuntoDeVentaAfip);
        try {
            FERecuperaLastCbteResponse response = afipWebServiceSOAPClient.FECompUltimoAutorizado(solicitud);
            return response.getCbteNro() + 1;
        } catch (WebServiceClientException ex) {
            LOGGER.error(ex.getMessage());            
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes").getString("mensaje_siguiente_nro_comprobante_error"));
        }
    }
    
    @Override
    public FECAERequest transformComprobanteToFECAERequest(ComprobanteAFIP comprobante, int siguienteNroComprobante, int nroPuntoDeVentaAfip) {
        FECAERequest fecaeRequest = new FECAERequest();        
        FECAECabRequest cabecera = new FECAECabRequest();
        FECAEDetRequest detalle = new FECAEDetRequest();        
        // CbteTipo = 1: Factura A, 2: Nota de Débito A, 3: Nota de Crédito A, 6: Factura B, 7: Nota de Débito B, 8: Nota de Crédito B. 11: Factura C
        // DocTipo = 80: CUIT, 86: CUIL, 96: DNI, 99: Doc.(Otro)
        switch (comprobante.getTipoComprobante()) {
            case FACTURA_A:
                cabecera.setCbteTipo(1);
                detalle.setDocTipo(80);
                detalle.setDocNro(Long.valueOf(comprobante.getCliente().getIdFiscal().replace("-", "")));
                break;
            case NOTA_DEBITO_A:
                cabecera.setCbteTipo(2);
                detalle.setDocTipo(80);
                detalle.setDocNro(Long.valueOf(comprobante.getCliente().getIdFiscal().replace("-", "")));
                break;
            case NOTA_CREDITO_A:
                cabecera.setCbteTipo(3);
                detalle.setDocTipo(80);
                detalle.setDocNro(Long.valueOf(comprobante.getCliente().getIdFiscal().replace("-", "")));
                break;
            case FACTURA_B:
                cabecera.setCbteTipo(6);
                // menor a $1000, si DocTipo = 99 DocNro debe ser igual a 0 (simula un consumidor final ???)
                if (comprobante.getTotal().compareTo(new BigDecimal("1000")) < 0) { 
                    detalle.setDocTipo(99);
                    detalle.setDocNro(0);
                } else {
                    if (comprobante.getCliente().getIdFiscal().equals("")) {
                        throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes").getString("mensaje_cliente_sin_idFiscal_error"));
                    }
                    detalle.setDocTipo(80);
                    detalle.setDocNro(Long.valueOf(comprobante.getCliente().getIdFiscal().replace("-", "")));
                }
                break;
            case NOTA_DEBITO_B:
                cabecera.setCbteTipo(7);
                // menor a $1000, si DocTipo = 99 DocNro debe ser igual a 0 (simula un consumidor final ???)
                if (comprobante.getTotal().compareTo(new BigDecimal("1000")) < 0) {
                    detalle.setDocTipo(99);
                    detalle.setDocNro(0);
                } else {
                    if (comprobante.getCliente().getIdFiscal().equals("")) {
                        throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes").getString("mensaje_cliente_sin_idFiscal_error"));
                    }
                    detalle.setDocTipo(80);
                    detalle.setDocNro(Long.valueOf(comprobante.getCliente().getIdFiscal().replace("-", "")));
                }
                break;
            case NOTA_CREDITO_B:
                cabecera.setCbteTipo(8);
                // menor a $1000, si DocTipo = 99 DocNro debe ser igual a 0 (simula un consumidor final ???)
                if (comprobante.getTotal().compareTo(new BigDecimal("1000")) < 0) {
                    detalle.setDocTipo(99);
                    detalle.setDocNro(0);
                } else {
                    if (comprobante.getCliente().getIdFiscal().equals("")) {
                        throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes").getString("mensaje_cliente_sin_idFiscal_error"));
                    }
                    detalle.setDocTipo(80);
                    detalle.setDocNro(Long.valueOf(comprobante.getCliente().getIdFiscal().replace("-", "")));
                }
                break;
            case FACTURA_C:
                cabecera.setCbteTipo(11);
                detalle.setDocTipo(0);
                detalle.setDocNro(0);
                break;
        }
        cabecera.setCantReg(1); // Cantidad de registros del detalle del comprobante o lote de comprobantes de ingreso
        cabecera.setPtoVta(nroPuntoDeVentaAfip); // Punto de Venta del comprobante que se está informando. Si se informa más de un comprobante, todos deben corresponder al mismo punto de venta
        fecaeRequest.setFeCabReq(cabecera);
        ArrayOfFECAEDetRequest arrayDetalle = new ArrayOfFECAEDetRequest();        
        detalle.setCbteDesde(siguienteNroComprobante);
        detalle.setCbteHasta(siguienteNroComprobante);
        detalle.setConcepto(1); // Concepto del Comprobante. Valores permitidos: 1 Productos, 2 Servicios, 3 Productos y Servicios        
        detalle.setCbteFch(formatterFechaHora.format(comprobante.getFecha()).replace("/", "")); // Fecha del comprobante (yyyymmdd)        
        ArrayOfAlicIva arrayIVA = new ArrayOfAlicIva();
        if (comprobante.getIva21neto().compareTo(BigDecimal.ZERO) != 0) {
            AlicIva alicIVA21 = new AlicIva();
            alicIVA21.setId(5); // Valores: 5 (21%), 4 (10.5%)  
            alicIVA21.setBaseImp(comprobante.getIva21neto().multiply(new BigDecimal("100")).divide(new BigDecimal("21"), 2, RoundingMode.HALF_UP).doubleValue()); // Se calcula con: (100 * IVA_neto) / %IVA
            alicIVA21.setImporte(comprobante.getIva21neto().setScale(2, RoundingMode.HALF_UP).doubleValue()); 
            arrayIVA.getAlicIva().add(alicIVA21);
        }
        if (comprobante.getIva105neto().compareTo(BigDecimal.ZERO) != 0) {
            AlicIva alicIVA105 = new AlicIva();
            alicIVA105.setId(4); // Valores: 5 (21%), 4 (10.5%)
            alicIVA105.setBaseImp(comprobante.getIva105neto().multiply(new BigDecimal("100")).divide(new BigDecimal("21"), 2, RoundingMode.HALF_UP).doubleValue()); // Se calcula con: (100 * IVA_neto) / %IVA
            alicIVA105.setImporte(comprobante.getIva105neto().setScale(2, RoundingMode.HALF_UP).doubleValue());
            arrayIVA.getAlicIva().add(alicIVA105);
        }
        detalle.setIva(arrayIVA); // Array para informar las alícuotas y sus importes asociados a un comprobante <AlicIva>. Para comprobantes tipo C y Bienes Usados – Emisor Monotributista no debe informar el array.
        detalle.setImpIVA(comprobante.getIva105neto().add(comprobante.getIva21neto()).setScale(2, RoundingMode.HALF_UP).doubleValue()); // Suma de los importes del array de IVA. Para comprobantes tipo C debe ser igual a cero (0).        
        detalle.setImpNeto(comprobante.getSubtotalBruto().setScale(2, RoundingMode.HALF_UP).doubleValue()); // Importe neto gravado. Debe ser menor o igual a Importe total y no puede ser menor a cero. Para comprobantes tipo C este campo corresponde al Importe del Sub Total                
        detalle.setImpTotConc(comprobante.getMontoNoGravado().setScale(2, RoundingMode.HALF_UP).doubleValue()); // El campo “Importe neto no gravado” <ImpTotConc>. No puede ser menor a cero(0). Para comprobantes tipo C debe ser igual a cero (0).
        detalle.setImpTotal(comprobante.getTotal().setScale(2, RoundingMode.HALF_UP).doubleValue()); // Importe total del comprobante, Debe ser igual a Importe neto no gravado + Importe exento + Importe neto gravado + todos los campos de IVA al XX% + Importe de tributos                        
        detalle.setMonId("PES"); // Código de moneda del comprobante. Consultar método FEParamGetTiposMonedas para valores posibles
        detalle.setMonCotiz(1); // Cotización de la moneda informada. Para PES, pesos argentinos la misma debe ser 1
        arrayDetalle.getFECAEDetRequest().add(detalle);                               
        fecaeRequest.setFeDetReq(arrayDetalle);
        return fecaeRequest;
    }

}