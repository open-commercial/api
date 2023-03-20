package sic.service;

import afip.wsfe.wsdl.FEAuthRequest;
import afip.wsfe.wsdl.FECAERequest;
import sic.domain.ComprobanteAFIP;
import sic.entity.Sucursal;
import sic.domain.TipoDeComprobante;

public interface IAfipService {

    void autorizar(ComprobanteAFIP comprobante);
    
    FEAuthRequest getFEAuth(String afipNombreServicio, Sucursal sucursal);
    
    int getSiguienteNroComprobante(FEAuthRequest feAuthRequest, TipoDeComprobante tipo, int nroPuntoDeVentaAfip);
    
    FECAERequest transformComprobanteToFECAERequest(ComprobanteAFIP comprobante, int siguienteNroComprobante, int nroPuntoDeVentaAfip);
    
}
